package com.example.sugarsync;

import static androidx.fragment.app.FragmentManager.TAG;

//import com.evrencoskun.TableView;



//import java.awt.image.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
//import com.github.ooftf.tableview.TableView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;


import androidx.camera.core.ImageCaptureException;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import net.sourceforge.tess4j.Tesseract;


import android.view.View;
import android.view.ViewGroup;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GlucoseFragment extends Fragment {

    private PreviewView previewView;
    private Button bCapture;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;

    private ImageView capturedImageView;
    private TextView extractedTextView;

    private LineChart lineChart;

    private List<Entry> glucoseEntries = new ArrayList<>();

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("glucoseLevel");


    private Camera camera;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    private boolean isPreviewOpen = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_glucose, container, false);

        lineChart = view.findViewById(R.id.lineChart);
        configureLineChart();
        previewView = view.findViewById(R.id.previewView);
        bCapture = view.findViewById(R.id.bCapture);
        capturedImageView = view.findViewById(R.id.capturedImageView);
        extractedTextView = view.findViewById(R.id.extractedTextView);

        bCapture.setOnClickListener(v -> {
            if (!isPreviewOpen) {
                checkCameraPermission();


                ViewGroup.LayoutParams layoutParams = previewView.getLayoutParams();
                layoutParams.height = getResources().getDisplayMetrics().heightPixels / 2;
                previewView.setLayoutParams(layoutParams);

                previewView.setVisibility(View.VISIBLE);
                capturedImageView.setVisibility(View.GONE);
                isPreviewOpen = true;
            } else {
                capturePhoto();
            }
        });

        return view;
    }

    private void configureLineChart() {
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.getDescription().setEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new DateAxisValueFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        updateLineChartWithData();
    }

    private void updateLineChartWithData() {
        // Add new entry to the LineChart
        glucoseEntries.add(new Entry(glucoseEntries.size(), 5));

        // Create a LineDataSet and set data
        LineDataSet dataSet = new LineDataSet(glucoseEntries, "Glucose Levels");
        dataSet.setColor(Color.BLUE); // Set line color
        dataSet.setValueTextColor(Color.BLACK); // Set text color

        LineData lineData = new LineData(dataSet);

        // Set data to LineChart
        lineChart.setData(lineData);

        // Notify LineChart that data has changed
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            requestCameraPermission();
        } else {
            initializeCamera();
        }
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE
        );
    }

    private void initializeCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        try {
            cameraProvider.unbindAll();
            Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
            new Handler().postDelayed(this::capturePhoto, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void capturePhoto() {
        long timestamp = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        if (imageCapture != null) {
            imageCapture.takePicture(
                    new ImageCapture.OutputFileOptions.Builder(
                            requireContext().getContentResolver(),
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                    ).build(),
                    ContextCompat.getMainExecutor(requireContext()),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            Uri savedUri = outputFileResults.getSavedUri();
                            if (savedUri != null) {
                                capturedImageView.setImageURI(savedUri);
                                capturedImageView.setVisibility(View.GONE);  // Hide the captured image view
                                previewView.setVisibility(View.GONE);  // Hide the camera preview
                                extractTextFromImage(savedUri);
                                isPreviewOpen = false;

                                new Handler().postDelayed(() -> {
                                    capturedImageView.setVisibility(View.GONE);
                                }, 3000);
                            } else {
                                Toast.makeText(requireContext(), "Photo has been saved successfully.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Toast.makeText(requireContext(), "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        } else {
            Toast.makeText(requireContext(), "Error taking photo: ImageCapture is null", Toast.LENGTH_SHORT).show();
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    @SuppressLint("RestrictedApi")
    private void extractTextFromImage(Uri savedUri) {
        new Handler().postDelayed(() -> {
            try {
                Bitmap capturedBitmap = BitmapFactory.decodeStream(requireContext().getContentResolver().openInputStream(savedUri));

                // Check if the bitmap is null
                if (capturedBitmap == null) {
                    Log.e(TAG, "Failed to decode bitmap from URI: " + savedUri);
                    return;
                }

                TextRecognizer textRecognizer = new TextRecognizer.Builder(requireContext()).build();

                if (!textRecognizer.isOperational()) {
                    Log.e(TAG, "TextRecognizer is not operational");
                    return;
                }

                Frame frame = new Frame.Builder().setBitmap(capturedBitmap).build();
                SparseArray<TextBlock> textBlocks = textRecognizer.detect(frame);

                StringBuilder extractedText = new StringBuilder();

                for (int index = 0; index < textBlocks.size(); index++) {
                    TextBlock textBlock = textBlocks.valueAt(index);
                    extractedText.append(textBlock.getValue());
                    extractedText.append("\n");
                }

                handler.post(() -> {
                    extractedTextView.setText("Glucose Level: " +  extractedText.toString().trim());
                    extractedTextView.setVisibility(View.VISIBLE);



                    pushToFirebase(extractedText.toString().trim());
                });
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found: " + e.getMessage());
                e.printStackTrace();
            }
        }, 1000); // 1000 milliseconds delay
    }




    private void pushToFirebase(String glucoseLevel) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();



        // Check if the user is authenticated
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Get the user's unique identifier (Firebase User ID)
            String userId = currentUser.getUid();

            // Create a reference for the user's glucose levels
            DatabaseReference userGlucoseRef = myRef.child("users").child(userId).child("glucoseLevels");

            // Use a unique identifier, such as timestamp, for each entry
            String timestamp = String.valueOf(System.currentTimeMillis());

            // Create a new reference with the unique identifier
            DatabaseReference glucoseRef = userGlucoseRef.child(timestamp);

            // Set the glucose level as the value for the new reference
            glucoseRef.setValue(glucoseLevel);

            glucoseEntries.add(new Entry(glucoseEntries.size(), Float.parseFloat(glucoseLevel)));

            // Update LineChart with new data
            updateLineChartWithData();
        } else {
            // Handle the case where the user is not authenticated
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}


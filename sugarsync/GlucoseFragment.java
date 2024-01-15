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
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;


import androidx.appcompat.app.AlertDialog;
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

import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.sourceforge.tess4j.Tesseract;


import android.view.View;
import android.view.ViewGroup;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlucoseFragment extends Fragment implements OnChartValueSelectedListener {

    private PreviewView previewView;
    private Button bCapture;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;

    private ImageView capturedImageView;
    private TextView extractedTextView;

    private LineChart lineChart;

    private List<Entry> glucoseEntries = new ArrayList<>();
    private EditText etEntryDate;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("users");


    private Camera camera;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    private boolean isPreviewOpen = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_glucose, container, false);
        glucoseEntries = new ArrayList<>();
        lineChart = view.findViewById(R.id.lineChart);
       // configureLineChart();
        previewView = view.findViewById(R.id.previewView);
        bCapture = view.findViewById(R.id.bCapture);
        capturedImageView = view.findViewById(R.id.capturedImageView);
        extractedTextView = view.findViewById(R.id.extractedTextView);

        Button bAddManually = view.findViewById(R.id.bAddManually);
        bAddManually.setOnClickListener(v -> showManualEntryDialog());


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


        configureLineChart();
        fetchUserGlucoseData();

        return view;
    }

    private void showManualEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Manual Entry");

        // Set up the layout for the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_manual_entry, null);
        builder.setView(dialogView);

        // Find views in the dialog layout
        EditText etGlucoseLevel = dialogView.findViewById(R.id.etGlucoseLevel);
        EditText etEntryDate = dialogView.findViewById(R.id.etEntryDate);

        // Set up date picker for etEntryDate
        etEntryDate.setFocusable(false); // Prevent the keyboard from showing
        etEntryDate.setOnClickListener(v -> showDatePickerDialog());

        builder.setPositiveButton("Submit", (dialog, which) -> {
            // Get the entered values
            String glucoseLevel = etGlucoseLevel.getText().toString();
            String entryDate = etEntryDate.getText().toString();

            // Validate the input (you may add more validation as needed)
            if (!glucoseLevel.isEmpty() && !entryDate.isEmpty()) {
                // Convert entryDate to timestamp (you may use a suitable method)
                long timestamp = convertDateToTimestamp(entryDate);

                // Add the manual entry to the LineChart and Firebase
                glucoseEntries.add(new Entry(timestamp, Float.parseFloat(glucoseLevel)));
                updateLineChartWithData();
                pushManualEntryToFirebase(glucoseLevel, timestamp);
            } else {
                // Show an error message if input is invalid
                Toast.makeText(requireContext(), "Invalid input. Please enter both glucose level and date.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        builder.create().show();
    }

    private void showDatePickerDialog() {
        DatePickerFragment datePickerFragment = new DatePickerFragment(etEntryDate);
        datePickerFragment.show(getChildFragmentManager(), "datePicker");
    }



    private long convertDateToTimestamp(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Date parsedDate = sdf.parse(date);
            if (parsedDate != null) {
                return parsedDate.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis();  // Return current time if conversion fails
    }




    private void pushManualEntryToFirebase(String glucoseLevel, long timestamp) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Check if the user is authenticated
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Get the user's unique identifier (Firebase User ID)
            String userId = currentUser.getUid();

            // Create a reference for the user
            DatabaseReference userRef = myRef.child(userId);

            // Set the glucose level as the value for the new reference directly under the user's node
            DatabaseReference glucoseRef = userRef.child("glucoseLevels").child(String.valueOf(timestamp));

            // Set the glucose level as the value for the new reference
            glucoseRef.setValue(glucoseLevel);

            // Add the manual entry to the LineChart data
            glucoseEntries.add(new Entry(timestamp, Float.parseFloat(glucoseLevel)));

            // Update LineChart with new data
            updateLineChartWithData();
        } else {
            // Handle the case where the user is not authenticated
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }




    private void fetchUserGlucoseData() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d("Chart", "User authenticated. UID: " + userId);

            DatabaseReference userRef = myRef.child("glucoseLevels");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    glucoseEntries.clear();
                    Log.d("Chart", "Number of children under glucoseLevels: " + snapshot.getChildrenCount());
                    for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                        long timestamp = Long.parseLong(entrySnapshot.getKey());
                        String glucoseLevel = entrySnapshot.getValue(String.class);

                        // Add the fetched entry to the LineChart
                        glucoseEntries.add(new Entry(timestamp, Float.parseFloat(glucoseLevel)));
                        Log.d("Chart", "Entry: Timestamp=" + timestamp + ", GlucoseLevel=" + glucoseLevel);
                    }
                    Log.d("Chart", "Fetched " + glucoseEntries.size() + " entries from Firebase");
                    // Update LineChart with the fetched data
                    updateLineChartWithData();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                    Toast.makeText(requireContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    Log.e("Chart", "Failed to fetch data from Firebase: " + error.getMessage());
                }
            });
        }
    }


    private void configureLineChart() {
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.getDescription().setEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new DateAxisValueFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
       // updateLineChartWithData(glucoseEntries);

        lineChart.setOnChartValueSelectedListener(this);

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        // Display an alert dialog with information about the selected data point
        int selectedIndex = (int) e.getX();
        if (selectedIndex >= 0 && selectedIndex < glucoseEntries.size()) {
            Entry selectedEntry = glucoseEntries.get(selectedIndex);
            showDataPointInfo(selectedEntry);
        }
    }

    @Override
    public void onNothingSelected() {
        // Do nothing when no value is selected
    }

    private void showDataPointInfo(Entry selectedEntry) {
        // Extract information from the selected data point
        int selectedIndex = (int) selectedEntry.getX();
        float glucoseLevel = selectedEntry.getY();

        // Retrieve additional information as needed
        String additionalInfo = "Date: " + getDateFromTimestamp(selectedIndex); // Customize this based on your data

        // Show an alert dialog with the information
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Data Point Information")
                .setMessage("Glucose Level: " + glucoseLevel + "\n" + additionalInfo)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String getDateFromTimestamp(int timestamp) {
        long timestampInMillis = (long) timestamp;
        return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault()).format(new Date(timestampInMillis));
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

        // Configure scrolling and initial view position
        float visibleRange = 10.0f;  // Set the number of visible entries
        lineChart.setVisibleXRangeMinimum(visibleRange);
        lineChart.setVisibleXRangeMaximum(visibleRange);

        // Set the initial view position
        float initialViewPosition = Math.max(0, glucoseEntries.size() - visibleRange);
        lineChart.moveViewToX(initialViewPosition);

        // Notify LineChart that data has changed
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();

    }



    private int findEntryIndex(float value) {
        for (int i = 0; i < glucoseEntries.size(); i++) {
            if (glucoseEntries.get(i).getY() >= value) {
                return i;
            }
        }
        return -1;
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

                // Filter out only the numbers with a decimal dot
                for (int index = 0; index < textBlocks.size(); index++) {
                    TextBlock textBlock = textBlocks.valueAt(index);
                    String value = textBlock.getValue().trim();

                    // Exclude words with alphabetic characters
                    if (!value.matches(".*[a-zA-Z].*")) {
                        extractedText.append(value);

                    }
                }


                handler.post(() -> {
                    extractedTextView.setText("Glucose Level: " + extractedText.toString().trim());
                    extractedTextView.setVisibility(View.VISIBLE);

                    String numericValues = extractNumericValues(extractedText.toString().trim());
                    pushToFirebase(numericValues);

                    pushToFirebase(extractedText.toString().trim());
                });
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found: " + e.getMessage());
                e.printStackTrace();
            }
        }, 1000); // 1000 milliseconds delay
    }


    private String extractNumericValues(String input) {
        // Use a regular expression to match numeric values
        String regex = "\\b[0-9]+([.,][0-9]+)?\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        StringBuilder result = new StringBuilder();

        // Append all matched numeric values
        while (matcher.find()) {
            // Filter out non-numeric characters
            String numericValue = matcher.group().replaceAll("[^0-9.]", "");

            result.append(numericValue);
            result.append(" ");  // Add space between multiple numeric values
        }

        return result.toString().trim();
    }


    private void pushToFirebase(String glucoseLevel) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Check if the user is authenticated
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Get the user's unique identifier (Firebase User ID)
            String userId = currentUser.getUid();

            // Create a reference for the user
            DatabaseReference userRef = myRef.child(userId);

            // Set the glucose level as the value for the new reference directly under the user's node
            DatabaseReference glucoseRef = userRef.child("glucoseLevels").child(String.valueOf(System.currentTimeMillis()));

            // Set the glucose level as the value for the new reference
            glucoseRef.setValue(glucoseLevel);

            glucoseEntries.add(new Entry(glucoseEntries.size(), Float.parseFloat(glucoseLevel)));
            Log.d("Chart", "Added entry to glucoseEntries. Size: " + glucoseEntries.size());
            // Update LineChart with new data
            updateLineChartWithData();
        } else {
            // Handle the case where the user is not authenticated
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }

    }
}

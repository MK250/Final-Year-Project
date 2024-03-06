package com.example.sugarsync;

import static androidx.fragment.app.FragmentManager.TAG;

//import com.evrencoskun.TableView;



//import java.awt.image.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
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


import androidx.annotation.Nullable;
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
import com.google.firebase.database.Query;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
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

        //etEntryDate = view.findViewById(R.id.etEntryDate);

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

        fetchUserGlucoseData();
        configureLineChart();
        //updateLineChartWithData();
        //fetchUserGlucoseData();

        return view;
    }

    private void showManualEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Manual Entry");
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_manual_entry, null);
        builder.setView(dialogView);

        EditText etTime = dialogView.findViewById(R.id.etTime);
        EditText etGlucoseLevel = dialogView.findViewById(R.id.etGlucoseLevel);

        // Use a DatePicker to get the date
        DatePicker datePicker = dialogView.findViewById(R.id.datePicker);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String time = etTime.getText().toString();
            String glucoseLevel = etGlucoseLevel.getText().toString();

            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth();
            int year = datePicker.getYear();

            // Convert date and time to timestamp
            long timestamp = convertDateTimeToTimestamp(day, month, year, time);

            // Push the manual entry to Firebase
            pushManualEntryToFirebase(glucoseLevel, timestamp);
            updateLineChartWithData();

            // Update LineChart with new data
            //updateLineChartWithData();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private long convertDateTimeToTimestamp(int day, int month, int year, String time) {
        try {
            String dateTimeString = String.format(Locale.getDefault(), "%02d/%02d/%04d %s", month + 1, day, year, time);
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());

            Date dateTime = sdf.parse(dateTimeString);


            return dateTime.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }


    private String formatDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day); // month is zero-based
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }


    private long convertDateTimeToTimestamp(String date, String time) {
        try {
            String dateTimeString = date + " " + time;
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
            Date dateTime = sdf.parse(dateTimeString);
            return dateTime.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Ensure that the fragment has options menu
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_glucose_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_view_all) {
            // Handle the menu item click to open ViewAllGlucoseActivity
            startActivity(new Intent(requireContext(), ViewAllGlucoseActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            //   updateLineChartWithData();

            Toast.makeText(requireContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();


            //  Log.d("ManualEntry", "Timestamp: " + timestamp + ", Glucose Level: " + glucoseLevel);
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
        xAxis.setValueFormatter(new DateAxisValueFormatter()); // Set the DateAxisValueFormatter here
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

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
        // Sort glucoseEntries based on timestamps

        float glucoseLevel = 5.5f;

        glucoseEntries.add(new Entry(glucoseEntries.size(), glucoseLevel));
        Collections.sort(glucoseEntries, (entry1, entry2) -> Long.compare((long) entry1.getX(), (long) entry2.getX()));

        // Create a LineDataSet and set data
        LineDataSet dataSet = new LineDataSet(glucoseEntries, "Glucose Levels");
        dataSet.setColor(Color.BLUE); // Set line color
        dataSet.setValueTextColor(Color.BLACK); // Set text color

        LineData lineData = new LineData(dataSet);

        // Set data to LineChart
        lineChart.setData(lineData);

        // Configure X-axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new DateAxisValueFormatter()); // Assuming you have a DateAxisValueFormatter
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

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
        if (glucoseLevel.isEmpty()) {
            Toast.makeText(requireContext(), "Glucose level is empty", Toast.LENGTH_SHORT).show();
            return; // Don't proceed further if glucose level is empty
        }


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

            float glucoseValue = Float.parseFloat(glucoseLevel);
            if (glucoseValue > 8.0) {
                // Display alert for high glucose level
                showHighGlucoseAlert();
            } else if (glucoseValue > 6.0) {
                // If glucose level is more than 6.0, check sugar intake for the past 24 hours
                DatabaseReference dietRef = userRef.child("diet");
                DatabaseReference exerciseRef = userRef.child("exercise");

                final double[] totalSugarIntake = {0};
                final int[] totalExerciseTime = {0};

                // Get the current time and time 24 hours ago
                long currentTimeMillis = System.currentTimeMillis();
                long twentyFourHoursAgo = currentTimeMillis - (24 * 60 * 60 * 1000); // 24 hours ago

                dietRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("Firebase", "Fetched diet entries from Firebase: " + dataSnapshot.getChildrenCount());

                        // Iterate through diet entries to calculate total sugar intake
                        for (DataSnapshot dietSnapshot : dataSnapshot.getChildren()) {
                            // Iterate through meals in each diet entry
                            for (DataSnapshot mealSnapshot : dietSnapshot.getChildren()) {
                                // Check if the meal has a sugar field
                                if (mealSnapshot.hasChild("sugar")) {
                                    // Get the sugar amount for this meal
                                    Double sugarAmount = mealSnapshot.child("sugar").getValue(Double.class);
                                    // Check if sugarAmount is not null and greater than 0
                                    if (sugarAmount != null && sugarAmount > 0) {
                                        // Add sugarAmount to total sugar intake
                                        totalSugarIntake[0] += sugarAmount;
                                    }
                                    Log.d("Firebase", "Sugar Amount for this entry: " + sugarAmount);
                                }
                            }
                        }
                        Log.d("Firebase", "Total Sugar Intake: " + totalSugarIntake[0]); // Log total sugar intake

                        // Fetch exercise data
                        exerciseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Log.d("Firebase", "Fetched exercise entries from Firebase: " + snapshot.getChildrenCount());

                                // Iterate through exercise entries to calculate total exercise time
                                for (DataSnapshot exerciseSnapshot : snapshot.getChildren()) {
                                    Double exerciseTime = exerciseSnapshot.child("exerciseTime").getValue(Double.class);
                                    if (exerciseTime != null) {
                                        totalExerciseTime[0] += exerciseTime.intValue();
                                    }
                                }
                                Log.d("Firebase", "Total Exercise Time: " + totalExerciseTime[0]); // Log total exercise time

                                // Check if total sugar intake exceeds 30g and total exercise time is less than 30 minutes
                                if ( totalExerciseTime[0] < 30) {
                                    // Display the alert dialog with exercise and sugar intake information
                                    showExerciseAndSugarAlert(totalExerciseTime[0], totalSugarIntake[0]);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("Firebase", "Error fetching exercise data: " + error.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Firebase", "Error fetching diet data: " + databaseError.getMessage());
                    }
                });
            } else if (glucoseValue < 3.0) {
                // If glucose level is less than 3.0, notify the user about dangerously low sugar level
                showLowSugarLevelAlert();
            } else {
                // Handle the case where the user is not authenticated
                Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            }

// Update LineChart with new data
            glucoseEntries.add(new Entry(glucoseEntries.size(), Float.parseFloat(glucoseLevel)));
            updateLineChartWithData();

        } else {
// Handle the case where the user is not authenticated
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }

}

    private void showLowSugarLevelAlert() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Low Sugar Level")
                    .setMessage("Your glucose level is dangerously low. Please go to the hospital immediately.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            Log.d("AlertDialog", "Low sugar level alert dialog shown successfully.");
        } catch (Exception e) {
            Log.e("AlertDialog", "Error showing low sugar level alert dialog: " + e.getMessage());
        }
    }

    private void showHighGlucoseAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("High Glucose Alert")
                .setMessage("Your glucose level is above 8.0 mmol/L. Please consult with your healthcare provider.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Dismiss the dialog
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private void showExerciseAndSugarAlert(int totalExerciseTime, double totalSugarIntake) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Exercise and Sugar Intake")
                    .setMessage("Your glucose level is above 6.0, your total sugar intake has exceeded 30g "  + " grams, and your total exercise time is " + totalExerciseTime + " minutes.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            Log.d("AlertDialog", "Exercise and sugar intake alert dialog shown successfully.");
        } catch (Exception e) {
            Log.e("AlertDialog", "Error showing exercise and sugar intake alert dialog: " + e.getMessage());
        }
    }


}
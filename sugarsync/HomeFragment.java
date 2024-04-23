package com.example.sugarsync;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";


    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;

    private BarChart barChart;

    private BarChart barChartSugarIntake;

    private BarChart barChartBloodGlucose;

    private float totalExerciseTime = 0;

    private Button buttonBolusAdvisor;

    private Button buttonSetTargetRanges;

    private TextView sugarIntakeTextView;

    //private TextView textExerciseMinutes;

    TextView textExerciseMinutes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.customActionBarColor)));
        }
        barChart = view.findViewById(R.id.barChartExercise);
        retrieveExerciseData();
        barChartSugarIntake = view.findViewById(R.id.barChartSugarIntake);
        retrieveSugarIntakeData();

        barChartBloodGlucose = view.findViewById(R.id.barChartBloodGlucose);
        retrieveBloodGlucoseData( );

        buttonBolusAdvisor = view.findViewById(R.id.buttonBolusAdvisor);

        // Fetch and display the latest glucose reading
        TextView glucoseReadingTextView = view.findViewById(R.id.textGlucoseReading);
        retrieveGlucoseReading(glucoseReadingTextView);

        //sugarIntakeTextView = view.findViewById(R.id.textSugarReading);
        TextView textSugarReading = view.findViewById(R.id.textSugarReading);
        // Assuming sugarIntakeTextView is declared as a field in your fragment


        buttonSetTargetRanges = view.findViewById(R.id.buttonSetTargetRanges);

        buttonSetTargetRanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call a method to show the alert dialog
                showTargetInputDialog();
            }
        });


        retrieveLatestSugarIntake(textSugarReading);

        TextView textExerciseMinutes = view.findViewById(R.id.textExerciseMinutes);
        retrieveExerciseDataCircle(textExerciseMinutes);


        // Set click listener for the button
        buttonBolusAdvisor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the BolusAdvisorActivity when the button is clicked
                startActivity(new Intent(getActivity(), BolusAdvisorActivity.class));
            }
        });

        TextView latestCheckTextView = view.findViewById(R.id.textLatestCheck);
        retrieveLatestCheckTimestamp(latestCheckTextView);


        setupUI(view);

        setupUISugar(view);

        setupUIExercise(view);

        return view;
    }

    private void retrieveExerciseDataCircle(TextView textExerciseMinutes) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userId)
                    .child("exercise");

            // Query to get exercises ordered by timestamp in descending order
            Query query = databaseReference.orderByChild("timestamp").limitToLast(1);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot exerciseSnapshot : dataSnapshot.getChildren()) {
                            // Assuming your exercise data contains a "timestamp" field
                            Double exerciseTime = exerciseSnapshot.child("exerciseTime").getValue(Double.class);
                            if (exerciseTime != null) {
                                String exerciseTimeStr = String.valueOf(exerciseTime);
                                textExerciseMinutes.setText(exerciseTimeStr);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database Error: " + databaseError.getMessage());
                }
            });
        }
    }

    private void retrieveLatestSugarIntake(TextView textSugarReading) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userId)
                    .child("diets");

            // Query to get diets ordered by timestamp in descending order
            Query query = databaseReference.orderByChild("timestamp").limitToLast(1);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot dietSnapshot : dataSnapshot.getChildren()) {
                            // Assuming your diet data contains a "sugar" field
                            Double sugarIntake = dietSnapshot.child("sugar").getValue(Double.class);
                            if (sugarIntake != null) {
                                textSugarReading.setText(String.valueOf(sugarIntake) + "g");
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database Error: " + databaseError.getMessage());
                }
            });
        }
    }



    private void retrieveGlucoseReading(TextView glucoseReadingTextView) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userId)
                    .child("glucoseLevels");

            Query query = databaseReference
                    .orderByKey()
                    .limitToLast(1);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        DataSnapshot latestSnapshot = dataSnapshot.getChildren().iterator().next();
                        if (latestSnapshot != null) {
                            String glucoseReading = latestSnapshot.getValue(String.class);
                            if (glucoseReading != null) {
                                glucoseReadingTextView.setText(glucoseReading);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database Error: " + databaseError.getMessage());
                }
            });
        }
    }



    private void retrieveLatestCheckTimestamp(TextView latestCheckTextView) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userId)
                    .child("glucoseLevels");

            // Create a query to order by key and limit to last 1
            Query query = databaseReference.orderByKey().limitToLast(1);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Get the latest child snapshot
                        DataSnapshot latestSnapshot = dataSnapshot.getChildren().iterator().next();
                        if (latestSnapshot != null) {
                            String timestamp = latestSnapshot.getKey();
                            if (timestamp != null) {
                                String formattedTimestamp = getDateFromTimestamp1(timestamp);
                                latestCheckTextView.setText("Latest Check: " + formattedTimestamp);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database Error: " + databaseError.getMessage());
                }
            });
        }
    }



    private String getDateFromTimestamp1(String timestamp) {
        try {
            long tsLong = Long.parseLong(timestamp);
            Date date = new Date(tsLong);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return sdf.format(date);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing timestamp: " + timestamp);
            return null; // Return null if there's an error parsing the timestamp
        }
    }


    private static final int INTERVAL_LAST_7_DAYS = 7;
    private static final int INTERVAL_LAST_30_DAYS = 30;

    private int interval = 7;
    private void retrieveBloodGlucoseData() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userId)
                    .child("glucoseLevels");

            // Calculate the timestamp for the specified interval
            Calendar intervalStartCalendar = Calendar.getInstance();
            intervalStartCalendar.add(Calendar.DAY_OF_YEAR, -interval);
            long intervalStartTimestamp = intervalStartCalendar.getTimeInMillis();

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    float totalGlucoseLevel = 0;
                    Map<String, List<Float>> bloodGlucoseMap = new HashMap<>();

                    for (DataSnapshot glucoseSnapshot : dataSnapshot.getChildren()) {
                        String timestamp = glucoseSnapshot.getKey();
                        String date = getDateFromTimestamp(timestamp);

                        // Convert timestamp to long
                        long glucoseTimestamp = Long.parseLong(timestamp);

                        // Check if the date is within the specified interval
                        if (glucoseTimestamp >= intervalStartTimestamp) {
                            String glucoseLevelString = glucoseSnapshot.getValue(String.class);

                            // Convert glucose level from string to float
                            if (glucoseLevelString != null) {
                                try {
                                    Float glucoseLevel = Float.parseFloat(glucoseLevelString);

                                    if (date != null) {
                                        if (bloodGlucoseMap.containsKey(date)) {
                                            bloodGlucoseMap.get(date).add(glucoseLevel);
                                        } else {
                                            List<Float> glucoseLevels = new ArrayList<>();
                                            glucoseLevels.add(glucoseLevel);
                                            bloodGlucoseMap.put(date, glucoseLevels);
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    // Handle parsing error if glucose level cannot be parsed to float
                                    Log.e(TAG, "Error parsing glucose level: " + glucoseLevelString);
                                }
                            }
                        }
                    }

                    // Calculate average glucose level for each day
                    List<String> dates = new ArrayList<>();
                    List<Float> averageGlucoseLevels = new ArrayList<>();
                    for (Map.Entry<String, List<Float>> entry : bloodGlucoseMap.entrySet()) {
                        String date = entry.getKey();
                        List<Float> glucoseLevels = entry.getValue();
                        float sum = 0;
                        for (Float level : glucoseLevels) {
                            sum += level;
                        }
                        float averageGlucoseLevel = sum / glucoseLevels.size();
                        dates.add(date);
                        averageGlucoseLevels.add(averageGlucoseLevel);
                    }

                    displayBloodGlucoseColumnChart( currentUser,averageGlucoseLevels, dates);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity(), "Database Error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Method to set up UI and button click listeners
    private void setupUI(View view) {
        // Find buttons by their IDs using the fragment's view
        Button buttonLast7Days = view.findViewById(R.id.buttonLast7Days);
        Button buttonLast30Days = view.findViewById(R.id.buttonLast30Days);

        // Set click listeners for buttons
        buttonLast7Days.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interval = INTERVAL_LAST_7_DAYS; // Set interval here
                retrieveBloodGlucoseData();
            }
        });

        buttonLast30Days.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interval = INTERVAL_LAST_30_DAYS; // Set interval here
                retrieveBloodGlucoseData();
            }
        });

        retrieveBloodGlucoseData();
    }





    private String getDateFromTimestamp(String timestamp) {
        try {
            long tsLong = Long.parseLong(timestamp);
            Date date = new Date(tsLong);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(date);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing timestamp: " + timestamp);
            return null; // Return null if there's an error parsing the timestamp
        }
    }


    private void showTargetInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_target_input, null);
        builder.setView(dialogView);

        EditText editTextMorningTargetMin = dialogView.findViewById(R.id.editTextMorningTargetMin);
        EditText editTextMorningTargetMax = dialogView.findViewById(R.id.editTextMorningTargetMax);
        EditText editTextAge = dialogView.findViewById(R.id.editTextAge);
        Spinner spinnerDiabetesType = dialogView.findViewById(R.id.spinnerDiabetesType);
        EditText editTextWeight = dialogView.findViewById(R.id.editTextWeight);
        EditText editTextAverageGlucose = dialogView.findViewById(R.id.editTextAverageGlucose);

        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);

        // Additional fields layout
        LinearLayout personalFieldsLayout = dialogView.findViewById(R.id.personalFieldsLayout);




// Sample data for the spinner
        String[] diabetesTypes = {"Type 1", "Type 2", "Gestational", "Other"};

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, diabetesTypes);

// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Apply the adapter to the spinner
        spinnerDiabetesType.setAdapter(adapter);


        // Add a listener to the Save button
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Check which option the user selected
                int selectedId = radioGroup.getCheckedRadioButtonId();
                String targetOption;
                if (selectedId == R.id.radioButtonDoctor) {
                    targetOption = "Doctor Recommended";
                } else {
                    targetOption = "Personal";
                }

                // Initialize morning min and max
                float morningMin = 0.0f;
                float morningMax = 0.0f;

                // Calculate or get the morning min and max based on the target option
                if (targetOption.equals("Personal")) {
                    // Parse additional user inputs
                    int age = Integer.parseInt(editTextAge.getText().toString());
                    String diabetesType = spinnerDiabetesType.getSelectedItem().toString();
                    float weight = Float.parseFloat(editTextWeight.getText().toString());
                    float averageGlucose = Float.parseFloat(editTextAverageGlucose.getText().toString());

                    // Perform calculation using the provided data
                    morningMin = calculateTargetMin(age, weight, diabetesType, averageGlucose);
                    morningMax = calculateTargetMax(age, weight, diabetesType, averageGlucose);
                } else if (targetOption.equals("Doctor Recommended")) {
                    // Parse entered morning min and max values
                    morningMin = Float.parseFloat(editTextMorningTargetMin.getText().toString());
                    morningMax = Float.parseFloat(editTextMorningTargetMax.getText().toString());
                }

                // Save the target ranges to Firebase
                saveTargetRangesToFirebase(morningMin, morningMax, targetOption);
            }
        });

        builder.setNegativeButton("Cancel", null);

        // Add a listener to the radio group
        // Add a listener to the radio group
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButtonPersonal) {
                    // Show the additional fields
                    personalFieldsLayout.setVisibility(View.VISIBLE);
                    // Hide morning min and max fields
                    editTextMorningTargetMin.setVisibility(View.GONE);
                    editTextMorningTargetMax.setVisibility(View.GONE);
                } else {
                    // Hide the additional fields
                    personalFieldsLayout.setVisibility(View.GONE);
                    // Show morning min and max fields

                }


            }
        });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Function to calculate the minimum target range based on age, weight, diabetes type, and average glucose reading
    // Function to calculate the minimum target range based on age, weight, diabetes type, and average glucose reading
    private float calculateTargetMin(int age, float weight, String diabetesType, float averageGlucose) {
        float targetMin;

        // Adjust target min based on age
        if (age < 30) {
            // Example: Lower target range for younger individuals
            targetMin = 4.0f;
        } else {
            // Example: Higher target range for older individuals
            targetMin = 4.5f;
        }

        // Adjust target min based on weight
        // Example: Increase target range for higher weight individuals
        targetMin += weight * 0.1f;

        // Adjust target min based on diabetes type
        // Example: Different target ranges for different types of diabetes
        if (diabetesType.equals("Type 1")) {
            // Example: Lower target range for type 1 diabetes
            targetMin -= 0.5f;
        } else if (diabetesType.equals("Type 2")) {
            // Example: Higher target range for type 2 diabetes
            targetMin += 0.5f;
        }

        // Adjust target min based on average glucose reading
        // Example: Increase target range if average glucose is high
        if (averageGlucose > 7.0f) {
            targetMin += 1.0f;
        }

        return targetMin;
    }

    // Function to calculate the maximum target range based on age, weight, diabetes type, and average glucose reading
    // Function to calculate the maximum target range based on age, weight, diabetes type, and average glucose reading
    private float calculateTargetMax(int age, float weight, String diabetesType, float averageGlucose) {
        // Example calculation:
        float targetMax = 0.0f;

        // Adjust target max based on age
        if (age < 30) {
            // Example: Lower target range for younger individuals
            targetMax = 8.0f;
        } else {
            // Example: Higher target range for older individuals
            targetMax = 9.0f;
        }

        // Adjust target max based on weight
        // Example: Increase target range for higher weight individuals
        targetMax += weight * 0.1f;

        // Adjust target max based on diabetes type
        // Example: Different target ranges for different types of diabetes
        if (diabetesType.equals("Type 1")) {
            // Example: Lower target range for type 1 diabetes
            targetMax -= 0.5f;
        } else if (diabetesType.equals("Type 2")) {
            // Example: Higher target range for type 2 diabetes
            targetMax += 0.5f;
        }

        // Adjust target max based on average glucose reading
        // Example: Increase target range if average glucose is high
        if (averageGlucose > 7.0f) {
            targetMax += 1.0f;
        }

        return targetMax;
    }




    private void saveTargetRangesToFirebase(float morningMin, float morningMax, String targetOption) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            DatabaseReference targetRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userId)
                    .child("target");

            Map<String, Object> targetMap = new HashMap<>();
            targetMap.put("morningMin", morningMin);
            targetMap.put("morningMax", morningMax);

            targetMap.put("targetOption", targetOption);

            targetRef.setValue(targetMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "Target ranges saved successfully", Toast.LENGTH_SHORT).show();
                            // Refresh the blood glucose chart after saving the target ranges
                            retrieveBloodGlucoseData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Failed to save target ranges", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private void displayBloodGlucoseColumnChart(FirebaseUser currentUser,List<Float> glucoseLevels, List<String> dates) {

        if (currentUser == null) {
            // Handle the case where the user is not authenticated
            return;
        }

        // Retrieve target ranges from Firebase
        DatabaseReference targetRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUser.getUid())
                .child("target");

        targetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    float morningMin = dataSnapshot.child("morningMin").getValue(Float.class);
                    float morningMax = dataSnapshot.child("morningMax").getValue(Float.class);


                    addLimitLineToAxis(barChartBloodGlucose, morningMin, morningMax,"Target Range", Color.RED);
                   // addLimitLineToAxis(barChartBloodGlucose, morningMax, "Evening Target Range", Color.GREEN);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });

        // Code to display the blood glucose chart remains the same as before
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < glucoseLevels.size(); i++) {
            entries.add(new BarEntry(i, glucoseLevels.get(i)));
        }

        BarDataSet barDataSet = new BarDataSet(entries, "Blood Glucose Levels");
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet);
        BarData data = new BarData(dataSets);

        barChartBloodGlucose.setData(data);
        barChartBloodGlucose.setFitBars(true); // make the bars fit the viewport width

        barChartBloodGlucose.invalidate();

        Description description = new Description();
        description.setText("Blood Glucose Levels");
        barChartBloodGlucose.setDescription(description);

        XAxis xAxis = barChartBloodGlucose.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setLabelRotationAngle(45); // Rotate labels for better visibility
        xAxis.setLabelCount(dates.size()); // Set label count to match the number of dates
        xAxis.setAvoidFirstLastClipping(true); // Avoid clipping of first and last labels





        // Enable touch gestures for scrolling
        barChartBloodGlucose.setTouchEnabled(true);
        barChartBloodGlucose.setDragEnabled(true);
        barChartBloodGlucose.setScaleEnabled(true);

        barChartBloodGlucose.animateY(2000); // animate the chart vertically
    }

    private void addLimitLineToAxis(BarLineChartBase<?> chart, float morningMin, float morningMax, String label, int color) {
        LimitLine morningLimitLine = new LimitLine(morningMax, label);
        morningLimitLine.setLineWidth(2f);
        morningLimitLine.setLineColor(color);
        morningLimitLine.enableDashedLine(10f, 10f, 0f);
        morningLimitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        morningLimitLine.setTextSize(10f);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.addLimitLine(morningLimitLine);
    }



    private int sugarInterval = 7;

    private void retrieveSugarIntakeData() {
        // Calculate the time interval within the method

        // You can modify interval here based on your requirements

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not logged in, handle accordingly
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference userDietRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("diets");

        Calendar intervalStartCalendar = Calendar.getInstance();
        intervalStartCalendar.add(Calendar.DAY_OF_YEAR, -sugarInterval);
        long intervalStartTimestamp = intervalStartCalendar.getTimeInMillis();

        userDietRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalSugarIntake = 0;
                Map<String, Double> sugarIntakeMap = new HashMap<>();

                for (DataSnapshot dietSnapshot : dataSnapshot.getChildren()) {
                    String date = dietSnapshot.child("date").getValue(String.class);
                    Double sugarIntake = dietSnapshot.child("sugar").getValue(Double.class);

                    if (date != null && sugarIntake != null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date currentDate = sdf.parse(date);
                            if (currentDate != null && currentDate.getTime() >= intervalStartTimestamp) {
                                // Aggregate sugar intake values for each date
                                if (sugarIntakeMap.containsKey(date)) {
                                    sugarIntakeMap.put(date, sugarIntakeMap.get(date) + sugarIntake);
                                } else {
                                    sugarIntakeMap.put(date, sugarIntake);
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Convert map to lists for chart display
                List<String> dates = new ArrayList<>(sugarIntakeMap.keySet());
                List<Double> sugarIntakeValues = new ArrayList<>(sugarIntakeMap.values());

                displaySugarIntakeColumnChart(sugarIntakeValues, dates);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupUISugar(View view) {
        Button buttonLast7Days = view.findViewById(R.id.buttonLast7DaysSugar); // Change button ID
        Button buttonLast30Days = view.findViewById(R.id.buttonLast30DaysSugar); // Change button ID


        buttonLast7Days.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sugarInterval = INTERVAL_LAST_7_DAYS;
                retrieveSugarIntakeData();
            }
        });

        buttonLast30Days.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sugarInterval = INTERVAL_LAST_30_DAYS;
                retrieveSugarIntakeData();
            }
        });

        retrieveSugarIntakeData();
    }


     private int exerciseInterval = 7;

    private void retrieveExerciseData() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userId)
                    .child("exercise");

            // Calculate the timestamp for the specified interval
            Calendar intervalStartCalendar = Calendar.getInstance();
            intervalStartCalendar.add(Calendar.DAY_OF_YEAR, -exerciseInterval);
            long intervalStartTimestamp = intervalStartCalendar.getTimeInMillis();

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Map<Long, Float> exerciseTimesPerDay = new HashMap<>();
                    List<String> dates = new ArrayList<>();
                    for (DataSnapshot exerciseSnapshot : dataSnapshot.getChildren()) {
                        if (exerciseSnapshot.child("date").exists() &&
                                exerciseSnapshot.child("exerciseTime").exists()) {
                            String date = exerciseSnapshot.child("date").getValue(String.class);
                            Float exerciseTime = exerciseSnapshot.child("exerciseTime").getValue(Float.class);
                            if (date != null && exerciseTime != null) {
                                long day = getDayFromDate(date);
                                // Check if the date is within the specified interval
                                if (day >= intervalStartTimestamp) {
                                    // Aggregate exercise times for each day
                                    if (exerciseTimesPerDay.containsKey(day)) {
                                        exerciseTimesPerDay.put(day, exerciseTimesPerDay.get(day) + exerciseTime);
                                    } else {
                                        exerciseTimesPerDay.put(day, exerciseTime);
                                        dates.add(date); // Add the date only once per day
                                    }
                                }
                            }
                        }
                    }
                    List<Float> exerciseTimes = new ArrayList<>(exerciseTimesPerDay.values());
                    displayColumnChart(exerciseTimes, dates);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity(), "Database Error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void setupUIExercise(View view) {
        Button buttonLast7Days = view.findViewById(R.id.buttonLast7DaysExercise); // Change button ID
        Button buttonLast30Days = view.findViewById(R.id.buttonLast30DaysExercise); // Change button ID


        buttonLast7Days.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exerciseInterval = INTERVAL_LAST_7_DAYS;
                retrieveExerciseData();
            }
        });

        buttonLast30Days.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exerciseInterval = INTERVAL_LAST_30_DAYS;
                retrieveExerciseData();
            }
        });

        retrieveExerciseData();
    }



    private void displaySugarIntakeColumnChart(List<Double> sugarIntakeValues, List<String> dates) {
        // Check if either list is empty
        if (sugarIntakeValues.isEmpty() || dates.isEmpty() || sugarIntakeValues.size() != dates.size()) {
            // Handle empty or mismatched data
            // For example, you can display a message or hide the chart
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < sugarIntakeValues.size(); i++) {
            entries.add(new BarEntry(i, sugarIntakeValues.get(i).floatValue())); // Convert to float
        }

        BarDataSet barDataSet = new BarDataSet(entries, "Sugar Intake");
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet);
        BarData data = new BarData(dataSets);

        barChartSugarIntake.setData(data);
        barChartSugarIntake.setFitBars(true); // make the bars fit the viewport width
        barChartSugarIntake.invalidate();

        Description description = new Description();
        description.setText("Sugar Intake for Last 7 Days");
        barChartSugarIntake.setDescription(description);

        XAxis xAxis = barChartSugarIntake.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setLabelRotationAngle(45);  // Rotate labels for better visibility
        xAxis.setLabelCount(dates.size()); // Set label count to match the number of dates
        xAxis.setAvoidFirstLastClipping(true); // Avoid clipping of first and last labels

        barChartSugarIntake.getAxisLeft().setAxisMinimum(0); // Set minimum value to 0

        // Set maximum value to the maximum sugar intake value + some buffer
        double maxSugarIntake = Collections.max(sugarIntakeValues);
        barChartSugarIntake.getAxisLeft().setAxisMaximum((float) (maxSugarIntake + 5)); // Adjust buffer as needed

        String targetLabel = "Target: 10";
        LimitLine limitLine = new LimitLine(10f, targetLabel);
        limitLine.setLineWidth(1f);
        limitLine.setLineColor(Color.RED);
        limitLine.setTextColor(Color.BLACK);
        limitLine.setTextSize(10f);

        YAxis leftAxis = barChartSugarIntake.getAxisLeft();
        leftAxis.removeAllLimitLines(); // Remove previous limit lines
        leftAxis.addLimitLine(limitLine);

        // Enable touch gestures for scrolling
        barChartSugarIntake.setTouchEnabled(true);
        barChartSugarIntake.setDragEnabled(true);
        barChartSugarIntake.setScaleEnabled(true);

        barChartSugarIntake.animateY(2000); // animate the chart vertically
    }


    private long getDayFromDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            // Clear hours, minutes, seconds, and milliseconds
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + dateString);
            return 0; // Return 0 if there's an error parsing the date
        }
    }


    private void displayColumnChart(List<Float> exerciseTimes, List<String> dates) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < exerciseTimes.size(); i++) {
            entries.add(new BarEntry(i, exerciseTimes.get(i)));
        }

        BarDataSet barDataSet = new BarDataSet(entries, "Exercise Times");
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet);
        BarData data = new BarData(dataSets);

        barChart.setData(data);
        barChart.setFitBars(true); // make the bars fit the viewport width
        barChart.invalidate();

        Description description = new Description();
        description.setText("Exercise Times");
        barChart.setDescription(description);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setLabelRotationAngle(45);  // Rotate labels for better visibility
        xAxis.setLabelCount(dates.size()); // Set label count to match the number of dates
        xAxis.setAvoidFirstLastClipping(true); // Avoid clipping of first and last labels

        barChart.getAxisLeft().setAxisMinimum(0); // Set minimum value to 0

        // Set maximum value to the maximum exercise time value + some buffer
        if (!exerciseTimes.isEmpty()) {
            // Set maximum value to the maximum exercise time value + some buffer
            float maxExerciseTime = Collections.max(exerciseTimes);
            barChart.getAxisLeft().setAxisMaximum(maxExerciseTime + 5); // Adjust buffer as needed
        }

        String targetLabel = "Target: 20";
        LimitLine limitLine = new LimitLine(20f, targetLabel);
        limitLine.setLineWidth(1f);
        limitLine.setLineColor(Color.RED);
        limitLine.setTextColor(Color.BLACK);
        limitLine.setTextSize(10f);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // Remove previous limit lines
        leftAxis.addLimitLine(limitLine);

        // Enable touch gestures for scrolling
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);

        barChart.animateY(2000); // animate the chart vertically
    }


}
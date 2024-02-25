package com.example.sugarsync;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
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



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        barChart = view.findViewById(R.id.barChartExercise);
        retrieveExerciseData();
        barChartSugarIntake = view.findViewById(R.id.barChartSugarIntake);
        retrieveSugarIntakeData();

        barChartBloodGlucose = view.findViewById(R.id.barChartBloodGlucose);
        retrieveBloodGlucoseData();




        return view;
    }


    private void retrieveBloodGlucoseData() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userId)
                    .child("glucoseLevels");

            // Calculate the timestamp for 7 days ago
            Calendar sevenDaysAgoCalendar = Calendar.getInstance();
            sevenDaysAgoCalendar.add(Calendar.DAY_OF_YEAR, -7);
            long sevenDaysAgoTimestamp = sevenDaysAgoCalendar.getTimeInMillis();

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Map<String, Float> bloodGlucoseMap = new HashMap<>();

                    for (DataSnapshot glucoseSnapshot : dataSnapshot.getChildren()) {
                        String timestamp = glucoseSnapshot.getKey();
                        String date = getDateFromTimestamp(timestamp);

                        // Convert timestamp to long
                        long glucoseTimestamp = Long.parseLong(timestamp);

                        // Check if the date is within the last 7 days
                        if (glucoseTimestamp >= sevenDaysAgoTimestamp) {
                            String glucoseLevelString = glucoseSnapshot.getValue(String.class);

                            // Convert glucose level from string to float
                            if (glucoseLevelString != null) {
                                try {
                                    Float glucoseLevel = Float.parseFloat(glucoseLevelString);

                                    // Aggregate glucose level values for each date
                                    if (date != null) {
                                        if (bloodGlucoseMap.containsKey(date)) {
                                            bloodGlucoseMap.put(date, bloodGlucoseMap.get(date) + glucoseLevel);
                                        } else {
                                            bloodGlucoseMap.put(date, glucoseLevel);
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    // Handle parsing error if glucose level cannot be parsed to float
                                    Log.e(TAG, "Error parsing glucose level: " + glucoseLevelString);
                                }
                            }
                        }
                    }

                    // Convert map to lists for chart display
                    List<String> dates = new ArrayList<>(bloodGlucoseMap.keySet());
                    List<Float> glucoseLevels = new ArrayList<>(bloodGlucoseMap.values());

                    displayBloodGlucoseColumnChart(glucoseLevels, dates);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity(), "Database Error", Toast.LENGTH_SHORT).show();
                }
            });
        }
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


    private void displayBloodGlucoseColumnChart(List<Float> glucoseLevels, List<String> dates) {
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
        xAxis.setLabelRotationAngle(0); // Rotate labels for better visibility
        xAxis.setLabelCount(dates.size()); // Set label count to match the number of dates

        xAxis.setTextSize(12f);

        barChartBloodGlucose.animateY(2000); // animate the chart vertically
    }


    private void retrieveSugarIntakeData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not logged in, handle accordingly
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference userDietRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("diets");

        userDietRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Double> sugarIntakeMap = new HashMap<>();

                // Calculate the timestamp for 7 days ago
                Calendar sevenDaysAgoCalendar = Calendar.getInstance();
                sevenDaysAgoCalendar.add(Calendar.DAY_OF_YEAR, -7);
                long sevenDaysAgoTimestamp = sevenDaysAgoCalendar.getTimeInMillis();

                for (DataSnapshot dietSnapshot : dataSnapshot.getChildren()) {
                    String date = dietSnapshot.child("date").getValue(String.class);
                    Double sugarIntake = dietSnapshot.child("sugar").getValue(Double.class);

                    if (date != null && sugarIntake != null) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date currentDate = sdf.parse(date);
                            if (currentDate != null && currentDate.getTime() >= sevenDaysAgoTimestamp) {
                                // Aggregate sugar intake values for each date

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




    private void retrieveExerciseData() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userId)
                    .child("exercise");

            // Calculate the timestamp for 7 days ago
            Calendar sevenDaysAgoCalendar = Calendar.getInstance();
            sevenDaysAgoCalendar.add(Calendar.DAY_OF_YEAR, -7);
            long sevenDaysAgoTimestamp = sevenDaysAgoCalendar.getTimeInMillis();

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Float> exerciseTimes = new ArrayList<>();
                    List<String> dates = new ArrayList<>();
                    for (DataSnapshot exerciseSnapshot : dataSnapshot.getChildren()) {
                        if (exerciseSnapshot.child("date").exists() &&
                                exerciseSnapshot.child("exerciseTime").exists()) {
                            String date = exerciseSnapshot.child("date").getValue(String.class);
                            Float exerciseTime = exerciseSnapshot.child("exerciseTime").getValue(Float.class);
                            if (date != null && exerciseTime != null) {
                                long day = getDayFromDate(date);
                                // Check if the date is within the last 7 days
                                if (day >= sevenDaysAgoTimestamp) {
                                    exerciseTimes.add(exerciseTime);
                                    dates.add(date);
                                    totalExerciseTime += exerciseTime;
                                }
                            }
                        }
                    }
                    displayColumnChart(exerciseTimes, dates);
                    updateExerciseProgressBar(totalExerciseTime);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity(), "Database Error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateExerciseProgressBar(float totalExerciseTime) {
        ProgressBar progressBar = getView().findViewById(R.id.progressExerciseTime);
        // Assuming the max value for progress bar is 100 (adjust accordingly if different)
        int maxProgress = 100;
        int progress = (int) ((totalExerciseTime / 60) * maxProgress); // Convert exercise time to minutes
        progressBar.setProgress(progress);
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
        xAxis.setLabelRotationAngle(20);  // Rotate labels for better visibility
        xAxis.setLabelCount(dates.size()); // Set label count to match the number of dates

        xAxis.setTextSize(5f);

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
        xAxis.setLabelRotationAngle(0);  // Rotate labels for better visibility
        xAxis.setLabelCount(dates.size()); // Set label count to match the number of dates

        xAxis.setTextSize(7f);

        barChart.animateY(2000); // animate the chart vertically
    }

}
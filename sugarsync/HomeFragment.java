package com.example.sugarsync;

import static android.content.ContentValues.TAG;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private BarChart barChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        barChart = view.findViewById(R.id.barChartExercise);
        retrieveExerciseData();
        return view;
    }

    private void retrieveExerciseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        // Calculate the timestamp for 7 days ago
        Calendar sevenDaysAgoCalendar = Calendar.getInstance();
        sevenDaysAgoCalendar.add(Calendar.DAY_OF_YEAR, -7);
        long sevenDaysAgoTimestamp = sevenDaysAgoCalendar.getTimeInMillis();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Float> exerciseTimes = new ArrayList<>();
                List<String> dates = new ArrayList<>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot exerciseSnapshot : userSnapshot.child("exercise").getChildren()) {
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
                                }
                            }
                        }
                    }
                }
                displayColumnChart(exerciseTimes, dates);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
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
        xAxis.setLabelRotationAngle(45); // Rotate labels for better visibility
        xAxis.setLabelCount(dates.size()); // Set label count to match the number of dates

        barChart.animateY(2000); // animate the chart vertically
    }

}
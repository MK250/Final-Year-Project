package com.example.sugarsync;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExerciseTimeXAxisValueFormatter extends ValueFormatter implements IAxisValueFormatter {

    private final List<Long> exerciseTimes; // List of exercise times in milliseconds

    public ExerciseTimeXAxisValueFormatter(List<Long> exerciseTimes) {
        this.exerciseTimes = exerciseTimes;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int index = (int) value;
        if (index >= 0 && index < exerciseTimes.size()) {
            long exerciseTimeMillis = exerciseTimes.get(index);
            // Convert milliseconds to a formatted time string
            return formatExerciseTime(exerciseTimeMillis);
        }
        return "";
    }

    private String formatExerciseTime(long exerciseTimeMillis) {
        Date date = new Date(exerciseTimeMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(date); // Format the time as "HH:mm"
    }
}

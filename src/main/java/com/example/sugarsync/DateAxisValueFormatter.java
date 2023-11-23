package com.example.sugarsync;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateAxisValueFormatter extends ValueFormatter {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        // Convert float value to a Date
        long millis = (long) value;
        Date date = new Date(millis);

        // Format the Date as a string and return
        return dateFormat.format(date);
    }
}


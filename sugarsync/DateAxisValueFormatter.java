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

        long millis = System.currentTimeMillis();
        Date date = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }
}
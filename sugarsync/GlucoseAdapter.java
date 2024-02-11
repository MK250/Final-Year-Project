package com.example.sugarsync;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class GlucoseAdapter extends RecyclerView.Adapter<GlucoseAdapter.GlucoseViewHolder> {

    private List<GlucoseEntry> glucoseEntries;

    public GlucoseAdapter(List<GlucoseEntry> glucoseEntries) {
        this.glucoseEntries = glucoseEntries;
    }

    @NonNull
    @Override
    public GlucoseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_glucose_level, parent, false);
        return new GlucoseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GlucoseViewHolder holder, int position) {
        GlucoseEntry glucoseEntry = glucoseEntries.get(position);
        holder.bind(glucoseEntry);

        // Show title only for the first entry
        if (position == 0) {
            holder.titleTextView.setVisibility(View.VISIBLE);
        } else {
            holder.titleTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return glucoseEntries.size();
    }

    public static class GlucoseViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView dateTextView;
        private final TextView timeTextView;
        private final TextView glucoseLevelTextView;

        public GlucoseViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            glucoseLevelTextView = itemView.findViewById(R.id.glucoseLevelTextView);
        }

        public void bind(GlucoseEntry glucoseEntry) {
            // Format the date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

            // Set the values to the TextViews
            dateTextView.setText("Date: " + dateFormat.format(glucoseEntry.getTimestamp()));
            timeTextView.setText("Time: " + timeFormat.format(glucoseEntry.getTimestamp()));
            glucoseLevelTextView.setText("Glucose Level: " + String.valueOf(glucoseEntry.getGlucoseLevel()));
        }
    }
}

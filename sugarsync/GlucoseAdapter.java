package com.example.sugarsync;

import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        return new GlucoseViewHolder(view, onDeleteClickListener);
    }
    @Override
    public void onBindViewHolder(@NonNull GlucoseViewHolder holder, int position) {
        GlucoseEntry glucoseEntry = glucoseEntries.get(position);
        holder.bind(glucoseEntry);


        if (position == 0) {
            holder.titleTextView.setVisibility(View.VISIBLE);
        } else {
            holder.titleTextView.setVisibility(View.GONE);
        }

        holder.deleteButton.setOnClickListener(view -> {
            // Get the timestamp of the glucose entry to delete
            long timestamp = glucoseEntries.get(position).getTimestamp();

            // Call the method to delete the item from Firebase
            ((ViewAllGlucoseActivity) holder.itemView.getContext()).deleteGlucoseItemFromFirebase(timestamp);

            // Remove the item from the RecyclerView dataset
            removeItem(position);
        });
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    private OnDeleteClickListener onDeleteClickListener;

    // Method to set the delete click listener
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    public void removeItem(int position) {
        if (position >= 0 && position < glucoseEntries.size()) {
            glucoseEntries.remove(position);
            notifyItemRemoved(position);
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

        private final Button editButton;

        private final Button deleteButton;

        public GlucoseViewHolder(@NonNull View itemView, GlucoseAdapter.OnDeleteClickListener onDeleteClickListener) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            glucoseLevelTextView = itemView.findViewById(R.id.glucoseLevelTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
           // this.onDeleteClickListener = onDeleteClickListener;

            deleteButton.setOnClickListener(v -> {
                if (onDeleteClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onDeleteClickListener.onDeleteClick(position);
                    }
                }
            });
        }

        public void bind(GlucoseEntry glucoseEntry) {
            // Format the date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

            // Set the values to the TextViews
            dateTextView.setText("Date: " + dateFormat.format(glucoseEntry.getTimestamp()));
            timeTextView.setText("Time: " + timeFormat.format(glucoseEntry.getTimestamp()));
            glucoseLevelTextView.setText("Glucose Level: " + String.valueOf(glucoseEntry.getGlucoseLevel()));

            editButton.setOnClickListener(view -> {
                // Call a method to handle edit action
                editGlucoseLevel(glucoseEntry);
            });


        }

        private void editGlucoseLevel(GlucoseEntry glucoseEntry) {

            Context context = itemView.getContext();
            Intent intent = new Intent(context, EditGlucoseActivity.class);
            intent.putExtra("glucoseEntry", glucoseEntry); // Pass glucose entry to the edit activity
            context.startActivity(intent);
        }
    }
}

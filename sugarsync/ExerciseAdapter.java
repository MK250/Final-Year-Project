package com.example.sugarsync;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    public interface OnEditClickListener {
        void onEditClick(int position, String field);
    }

    private List<Exercise> exerciseList;
    private OnEditClickListener onEditClickListener;

    // Constructor
    public ExerciseAdapter(OnEditClickListener onEditClickListener) {
        this.exerciseList = new ArrayList<>();
        this.onEditClickListener = onEditClickListener;
    }

    /*
    public void setOnEditClickListener(OnEditClickListener onEditClickListener) {
        this.onEditClickListener = onEditClickListener;
    }
*/
    // ViewHolder class
    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, exerciseTypeTextView, exerciseTimeTextView;
        ImageView editTypeButton, editTimeButton;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            exerciseTypeTextView = itemView.findViewById(R.id.exerciseTypeTextView);
            exerciseTimeTextView = itemView.findViewById(R.id.exerciseTimeTextView);
            editTypeButton = itemView.findViewById(R.id.editTypeButton);
            editTimeButton = itemView.findViewById(R.id.editTimeButton);




        }
    }

    // Add an exercise to the adapter
    public void addExercise(Exercise exercise) {
        // Ensure the exercise has a valid ID before adding to the list
        if (TextUtils.isEmpty(exercise.getId())) {
            exercise.setId(UUID.randomUUID().toString());
        }

        exerciseList.add(exercise);
        notifyItemInserted(exerciseList.size() - 1);
    }

    // Clear all exercises from the adapter
    public void clearExercises() {
        exerciseList.clear();
        notifyDataSetChanged();
    }

    public Exercise getExercise(int position) {
        if (position >= 0 && position < exerciseList.size()) {
            return exerciseList.get(position);
        } else {
            return null;
        }
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exercise_item_layout, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exerciseList.get(position);
        holder.dateTextView.setText("Date: " + exercise.getDate());
        holder.exerciseTypeTextView.setText("Exercise Type: " + exercise.getExerciseType());
        holder.exerciseTimeTextView.setText("Exercise Time: " + exercise.getExerciseTime());

        holder.editTypeButton.setOnClickListener(v -> {
            if (onEditClickListener != null) {
                // Ensure the exercise has a valid ID before triggering the edit dialog
                if (TextUtils.isEmpty(exercise.getId())) {
                    exercise.setId(UUID.randomUUID().toString());
                }
                onEditClickListener.onEditClick(position, "Exercise Type");
            }
        });

        holder.editTimeButton.setOnClickListener(v -> {
            if (onEditClickListener != null) {
                // Ensure the exercise has a valid ID before triggering the edit dialog
                if (TextUtils.isEmpty(exercise.getId())) {
                    exercise.setId(UUID.randomUUID().toString());
                }
                onEditClickListener.onEditClick(position, "Exercise Time");
            }
        });
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }
}
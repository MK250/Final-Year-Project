package com.example.sugarsync;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ViewAllExerciseActivity extends AppCompatActivity implements ExerciseAdapter.OnEditClickListener{

    private RecyclerView recyclerView;
    private ExerciseAdapter exerciseAdapter;

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_all_exercise);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.customActionBarColor)));
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        exerciseAdapter = new ExerciseAdapter(this);

        recyclerView.setAdapter(exerciseAdapter);

        //exerciseAdapter.setOnEditClickListener(this);
        //exerciseAdapter.setOnEditClickListener(ViewAllExerciseActivity.this); // Corrected line // Assuming your activity implements OnEditClickListener



        searchView = findViewById(R.id.searchView);

        // Setup search functionality
        setupSearchView();

        // Retrieve exercise data from Firebase and populate the adapter
       retrieveExerciseData();

    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search when user submits query (not used in this example)
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the adapter when text changes in the search bar
                exerciseAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }


    @Override
    public void onEditClick(int position, String field) {
        Exercise exercise = exerciseAdapter.getExercise(position);

        // Create an EditText and set its initial text to the existing value
        EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT); // or InputType.TYPE_CLASS_NUMBER for ExerciseTime
        editText.setText(field.equals("Exercise Type") ? exercise.getExerciseType() : String.valueOf(exercise.getExerciseTime()));

        // Create an AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("Edit " + field)
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newValue = editText.getText().toString();
                    updateExercise(position, exercise, field, newValue);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void updateExercise(int position, Exercise exercise, String field, String newValue) {
        // Update the Exercise based on the field (type or time)
        switch (field) {
            case "Exercise Type":
                exercise.setExerciseType(newValue);
                break;
            case "Exercise Time":
                // Assuming exercise time is a double, you might need error handling
                double newTime = Double.parseDouble(newValue);
                exercise.setExerciseTime(newTime);
                break;
        }

        updateExerciseEntry(position, exercise);
    }

    private void updateExerciseEntry(int position, Exercise exercise) {
        // Get user ID from Firebase authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not logged in, handle accordingly
            return;
        }

        String userId = currentUser.getUid();

        // Create a reference to the "users" node in Firebase
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Create a child node under "users" with the user ID
        DatabaseReference userExercisesRef = usersRef.child(userId).child("exercise");

        // Get the unique key for the selected exercise entry
        String exerciseKey = exercise.getId();

        // Create a reference to the specific exercise entry
        DatabaseReference exerciseRef = userExercisesRef.child(exerciseKey);

        // Create a new Exercise object with updated values
        Exercise updatedExercise = new Exercise(
                exercise.getDate(),
                exercise.getExerciseType(),
                exercise.getExerciseTime()
        );

        // Update the values in Firebase using the new Exercise object
        exerciseRef.setValue(updatedExercise).addOnSuccessListener(aVoid -> {
            // Notify the adapter that the specific item has changed
            exerciseAdapter.notifyItemChanged(position);

            // Display a toast message
            Toast.makeText(this, "Exercise entry updated", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            // Handle failure, if any
            Toast.makeText(this, "Failed to update exercise entry", Toast.LENGTH_SHORT).show();
        });
    }


    private void retrieveExerciseData() {
        // Get user ID from Firebase authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not logged in, handle accordingly
            return;
        }

        String userId = currentUser.getUid();

        // Retrieve exercise data from Firebase
        DatabaseReference exercisesRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId) // Use the user's unique ID here
                .child("exercise");

        exercisesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Exercise> exercises = new ArrayList<>();

                // Iterate through the exercises and add them to the adapter
                for (DataSnapshot exerciseSnapshot : dataSnapshot.getChildren()) {
                    Exercise exercise = exerciseSnapshot.getValue(Exercise.class);
                    if (exercise != null) {

                        exerciseAdapter.addExercise(exercise);
                    }
                }
                exerciseAdapter.setExerciseListFull(exercises);
                // Notify the adapter that the data set has changed

                exerciseAdapter.notifyDataSetChanged();

                recyclerView.post(() -> recyclerView.smoothScrollToPosition(exerciseAdapter.getItemCount() - 1));


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

}
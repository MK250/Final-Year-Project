package com.example.sugarsync;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditGlucoseActivity extends AppCompatActivity {

    private EditText glucoseLevelEditText;
    private Button saveButton;

    private GlucoseEntry glucoseEntry;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_glucose);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.customActionBarColor)));
        }

        glucoseLevelEditText = findViewById(R.id.glucoseLevelEditText);
        saveButton = findViewById(R.id.saveButton);

        // Get the GlucoseEntry object passed from the previous activity
        glucoseEntry = getIntent().getParcelableExtra("glucoseEntry");

        // Set the initial glucose level in the EditText
        glucoseLevelEditText.setText(String.valueOf(glucoseEntry.getGlucoseLevel()));

        saveButton.setOnClickListener(view -> saveChanges());
    }

    private void saveChanges() {
        // Get the edited glucose level from the EditText
        String newGlucoseLevelStr = glucoseLevelEditText.getText().toString().trim();
        if (newGlucoseLevelStr.isEmpty()) {
            Toast.makeText(this, "Please enter a glucose level", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse the new glucose level
        float newGlucoseLevel = Float.parseFloat(newGlucoseLevelStr);

        // Update the glucose level in the GlucoseEntry object
        glucoseEntry.setGlucoseLevel(newGlucoseLevel);

        // Update the glucose level in the Firebase database
        DatabaseReference glucoseRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("glucoseLevels").child(String.valueOf(glucoseEntry.getTimestamp()));

        glucoseRef.setValue(String.valueOf(newGlucoseLevel))
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase Update", "Glucose level updated successfully");
                    // Update successful
                    Toast.makeText(EditGlucoseActivity.this, "Glucose level updated successfully", Toast.LENGTH_SHORT).show();

                    // Pass the updated glucoseEntry and its position back to the previous activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedGlucoseEntry", glucoseEntry);
                    resultIntent.putExtra("position", getIntent().getIntExtra("position", -1));
                    setResult(RESULT_OK, resultIntent);
                    startActivity(new Intent(EditGlucoseActivity.this, ViewAllGlucoseActivity.class)); // Navigate to ViewAllGlucoseActivity
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Update failed
                    Toast.makeText(EditGlucoseActivity.this, "Failed to update glucose level", Toast.LENGTH_SHORT).show();
                });


    }

}


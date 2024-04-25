package com.example.sugarsync;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static java.security.AccessController.getContext;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ViewAllGlucoseActivity extends AppCompatActivity implements GlucoseAdapter.OnDeleteClickListener {

    private static final int EDIT_GLUCOSE_REQUEST = 1; // Request code for starting EditGlucoseActivity
    private ProgressBar loadingIndicator;
    private RecyclerView recyclerView;
    private GlucoseAdapter glucoseAdapter;
    private List<GlucoseEntry> glucoseEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_all_glucose);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.customActionBarColor)));
        }
        loadingIndicator = findViewById(R.id.loadingIndicator);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the glucoseEntries list
        glucoseEntries = new ArrayList<>();

        loadingIndicator.setVisibility(View.VISIBLE);
        // Fetch data from the Realtime Database and update the adapter
        fetchGlucoseData();

    }

    @Override
    public void onDeleteClick(int position) {
        // Show a confirmation dialog or directly delete the item
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this item?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Delete the item
            deleteGlucoseItemFromFirebase(position);
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

        public void deleteGlucoseItemFromFirebase(long timestamp) {
        DatabaseReference glucoseRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("glucoseLevels")
                .child(String.valueOf(timestamp));

        glucoseRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Deletion successful
                    Toast.makeText(this, "Glucose level deleted successfully", Toast.LENGTH_SHORT).show();
                    // Implement any additional logic here if needed
                })
                .addOnFailureListener(e -> {
                    // Deletion failed
                    Toast.makeText(this, "Failed to delete glucose level", Toast.LENGTH_SHORT).show();
                    // Implement any error handling logic here if needed
                });
    }


    private void fetchGlucoseData() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("FirebaseDataTrigger", "onDataChange triggered");
                    glucoseEntries.clear(); // Clear the existing entries before fetching new ones

                    DataSnapshot glucoseLevelsSnapshot = dataSnapshot.child("glucoseLevels");

                    for (DataSnapshot entrySnapshot : glucoseLevelsSnapshot.getChildren()) {
                        long timestamp = Long.parseLong(entrySnapshot.getKey());
                        String glucoseLevel = entrySnapshot.getValue(String.class);

                        // Check if glucoseLevel is not empty before parsing
                        if (!TextUtils.isEmpty(glucoseLevel)) {
                            // Create GlucoseEntry object and add it to the list
                            glucoseEntries.add(new GlucoseEntry(timestamp, Float.parseFloat(glucoseLevel)));
                        }
                    }

                    // Sort the list based on timestamp (oldest to latest)
                    Collections.sort(glucoseEntries, (entry1, entry2) -> Long.compare(entry1.getTimestamp(), entry2.getTimestamp()));

                    // Initialize and set up the adapter
                    glucoseAdapter = new GlucoseAdapter(glucoseEntries);
                    recyclerView.setAdapter(glucoseAdapter);
                    loadingIndicator.setVisibility(View.GONE);
                    recyclerView.post(() -> recyclerView.smoothScrollToPosition(glucoseAdapter.getItemCount() - 1));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                    Toast.makeText(ViewAllGlucoseActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_GLUCOSE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Retrieve the updated glucose entry and its position from the intent
            GlucoseEntry updatedEntry = data.getParcelableExtra("updatedGlucoseEntry");
            int position = data.getIntExtra("position", -1);

            // Update the corresponding entry in the list
            if (position != -1 && position < glucoseEntries.size()) {
                glucoseEntries.set(position, updatedEntry);

                // Notify the adapter of the change
                if (glucoseAdapter != null) {
                    glucoseAdapter.notifyItemChanged(position);

                    // Scroll to the updated position after RecyclerView updates
                  //  recyclerView.post(() -> recyclerView.smoothScrollToPosition(position));
                }
            }
        }
    }
}




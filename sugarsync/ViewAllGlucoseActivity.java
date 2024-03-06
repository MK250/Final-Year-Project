package com.example.sugarsync;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static java.security.AccessController.getContext;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
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


public class ViewAllGlucoseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GlucoseAdapter glucoseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_all_glucose);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch data from the Realtime Database and update the adapter
        fetchGlucoseData();


    }

    private void fetchGlucoseData() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<GlucoseEntry> glucoseEntries = new ArrayList<>();

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


}

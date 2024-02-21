package com.example.sugarsync;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class ViewAllDietsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DietAdapter dietAdapter;
    private TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_all_diet_activity);

        titleTextView = findViewById(R.id.titleTextView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dietAdapter = new DietAdapter();
        recyclerView.setAdapter(dietAdapter);



        dietAdapter.setOnDeleteClickListener((position, diet, mealType) -> showDeleteConfirmationDialog(position, diet, "all"));


        dietAdapter.setOnEditClickListener((position, diet, mealType) ->
                showEditDialog(position, diet, mealType));

        // Get the logged-in user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Get a reference to the "users" node in Firebase
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

            // Get a reference to the "diets" node for the logged-in user
            DatabaseReference userDietsRef = usersRef.child(userId).child("diets");

            // Attach a ValueEventListener to retrieve and display diets
            userDietsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Clear existing diets
                    dietAdapter.clearDiets();

                    // Iterate through the diets and add them to the adapter
                    for (DataSnapshot dietSnapshot : dataSnapshot.getChildren()) {
                        Diet diet = dietSnapshot.getValue(Diet.class);
                        if (diet != null) {
                            dietAdapter.addDiet(diet);
                        }
                    }

                    // Notify the adapter that the data set has changed
                    dietAdapter.notifyDataSetChanged();

                    // Update the title if there are diets
                    if (dietAdapter.getItemCount() > 0) {
                        titleTextView.setText("Diet");
                    } else {
                        titleTextView.setText("No Diets Found");
                    }

                    recyclerView.post(() -> recyclerView.smoothScrollToPosition(dietAdapter.getItemCount() - 1));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database error
                }
            });
        }
    }

    private void showEditDialog(int position, Diet diet, String mealType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit " + mealType);
        EditText editText = new EditText(this);
        editText.setText(getMealValue(diet, mealType));
        builder.setView(editText);

        builder.setPositiveButton("Save", (dialog, which) -> {
            updateDiet(position, diet, mealType, editText.getText().toString());
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private String getMealValue(Diet diet, String mealType) {
        switch (mealType) {
            case "breakfast":
                return diet.getBreakfast();
            case "lunch":
                return diet.getLunch();
            case "dinner":
                return diet.getDinner();
            default:
                return "";
        }
    }

    private void updateDiet(int position, Diet diet, String mealType, String newValue) {
        switch (mealType) {
            case "breakfast":
                diet.setBreakfast(newValue);
                break;
            case "lunch":
                diet.setLunch(newValue);
                break;
            case "dinner":
                diet.setDinner(newValue);
                break;
        }

        updateDietEntry(position, diet);
    }

    private void updateDietEntry(int position, Diet diet) {
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
        DatabaseReference userDietsRef = usersRef.child(userId).child("diets");

        // Get the unique key (dietId) for the selected diet entry
        String dietId = diet.getId(); // Use the ID directly from the Diet object

        // Create a reference to the specific diet entry
        DatabaseReference dietRef = userDietsRef.child(dietId);

        // Update the values in Firebase
        dietRef.setValue(diet).addOnSuccessListener(aVoid -> {
            // Notify the adapter that the data set has changed
            dietAdapter.notifyDataSetChanged();

            // Display a toast message
            Toast.makeText(this, "Diet entry updated", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            // Handle failure, if any
            Toast.makeText(this, "Failed to update diet entry", Toast.LENGTH_SHORT).show();
        });
    }


    private void showDeleteConfirmationDialog(int position, Diet diet, String mealType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this entry?");
        builder.setPositiveButton("Delete", (dialog, which) -> deleteDietEntry(position, diet, mealType));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteDietEntry(int position, Diet diet, String mealType) {
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
        DatabaseReference userDietsRef = usersRef.child(userId).child("diets");

        // Get the unique key (dietId) for the selected diet entry
        String dietId = diet.getId(); // Use the ID directly from the Diet object

        // Create a reference to the specific diet entry
        DatabaseReference dietRef = userDietsRef.child(dietId);

        // Remove the specific meal type from the diet entry
        diet.removeMealType(mealType);

        // Update the values in Firebase
        dietRef.setValue(diet).addOnSuccessListener(aVoid -> {
            // Display a toast message
            Toast.makeText(this, "Diet entry updated", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            // Handle failure, if any
            Toast.makeText(this, "Failed to update diet entry", Toast.LENGTH_SHORT).show();
        });
    }

}


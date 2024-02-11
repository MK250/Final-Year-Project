package com.example.sugarsync;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {

    private FirebaseAuth fAuth;
    private FirebaseUser mUser;
    private DatabaseReference userReference;

    // Declare TextViews to display user information
    private TextView emailTextView;
    private TextView diabetesTypeTextView;
    private TextView insulinTherapyTextView;
    private TextView pillsTextView;
    private TextView unitsTextView;
    private TextView targetRangeTextView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fAuth = FirebaseAuth.getInstance();
        mUser = fAuth.getCurrentUser();

        if (mUser != null) {
            String userID = mUser.getUid();
            userReference = FirebaseDatabase.getInstance().getReference("users").child(userID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize TextViews
        emailTextView = view.findViewById(R.id.emailTextView);
        diabetesTypeTextView = view.findViewById(R.id.diabetesTypeTextView);
        insulinTherapyTextView = view.findViewById(R.id.insulinTherapyTextView);
        pillsTextView = view.findViewById(R.id.pillsTextView);
        unitsTextView = view.findViewById(R.id.unitsTextView);
        targetRangeTextView = view.findViewById(R.id.targetRangeTextView);

        // Fetch and display user information
        if (userReference != null) {

            String email = mUser.getEmail();
            // Update email TextView
            emailTextView.setText("Email: " + email);
            // Fetch email separately

            // Fetch selected options
            userReference.child("selectedOptions").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get selected options from Firebase
                    String diabetesType = dataSnapshot.child("Question 1").getValue(String.class);
                    String insulinTherapy = dataSnapshot.child("Question 2").getValue(String.class);
                    String pills = dataSnapshot.child("Question 3").getValue(String.class);
                    String units = dataSnapshot.child("Question 4").getValue(String.class);
                    String targetRange = dataSnapshot.child("Question 5").getValue(String.class);

                    // Update TextViews with selected options
                    diabetesTypeTextView.setText("Diabetes Type: " + diabetesType);
                    insulinTherapyTextView.setText("Insulin Therapy: " + insulinTherapy);
                    pillsTextView.setText("Pills: " + pills);
                    unitsTextView.setText("Units for Blood Sugar: " + units);
                    targetRangeTextView.setText("Target Range: " + targetRange);

                    ImageView editIcon = view.findViewById(R.id.editIcon);
                    editIcon.setOnClickListener(v -> showEditDialog(diabetesType, insulinTherapy, pills, units, targetRange));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle error
                }
            });
        }


        // Find the "Log Off" button
        Button logOffButton = view.findViewById(R.id.btnLogOff);

        // Set click listener for the button
        logOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle logout logic here
                // For example, you can start a LoginActivity to log the user out
                Intent intent = new Intent(getActivity(), Login_Activity.class);
                startActivity(intent);
                getActivity().finish(); // Optional: Close the current activity if needed
            }
        });

        return view;
    }

    private void showEditDialog(String diabetesType, String insulinTherapy, String pills, String units, String targetRange) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Profile");

        // Inflate the custom layout for the dialog
        View editDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_profile_dialog, null);
        builder.setView(editDialogView);

        // Initialize EditTexts in the custom layout
        EditText diabetesTypeEditText = editDialogView.findViewById(R.id.editDiabetesType);
        EditText insulinTherapyEditText = editDialogView.findViewById(R.id.editInsulinTherapy);
        EditText pillsEditText = editDialogView.findViewById(R.id.editPills);
        EditText unitsEditText = editDialogView.findViewById(R.id.editUnits);
        EditText targetRangeEditText = editDialogView.findViewById(R.id.editTargetRange);

        // Set the initial values in EditTexts
        diabetesTypeEditText.setText(diabetesType);
        insulinTherapyEditText.setText(insulinTherapy);
        pillsEditText.setText(pills);
        unitsEditText.setText(units);
        targetRangeEditText.setText(targetRange);

        // Set positive button to save changes
        builder.setPositiveButton("Save", (dialog, which) -> {
            // Get the edited values
            String editedDiabetesType = diabetesTypeEditText.getText().toString();
            String editedInsulinTherapy = insulinTherapyEditText.getText().toString();
            String editedPills = pillsEditText.getText().toString();
            String editedUnits = unitsEditText.getText().toString();
            String editedTargetRange = targetRangeEditText.getText().toString();

            // Update the values in Firebase
            updateProfileData(editedDiabetesType, editedInsulinTherapy, editedPills, editedUnits, editedTargetRange);
        });

        // Set negative button to cancel
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        builder.create().show();
    }

    // Method to update the profile data in Firebase
    private void updateProfileData(String diabetesType, String insulinTherapy, String pills, String units, String targetRange) {
        // Get user ID from Firebase authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not logged in, handle accordingly
            return;
        }

        String userId = currentUser.getUid();

        // Update the values in Firebase
        userReference.child("selectedOptions").child("Question 1").setValue(diabetesType);
        userReference.child("selectedOptions").child("Question 2").setValue(insulinTherapy);
        userReference.child("selectedOptions").child("Question 3").setValue(pills);
        userReference.child("selectedOptions").child("Question 4").setValue(units);
        userReference.child("selectedOptions").child("Question 5").setValue(targetRange);

        // Notify the user that changes are saved
        Toast.makeText(requireContext(), "Changes saved", Toast.LENGTH_SHORT).show();
    }
}
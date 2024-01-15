package com.example.sugarsync;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


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
            userReference.child("selectedOptions").addListenerForSingleValueEvent(new ValueEventListener() {
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
}
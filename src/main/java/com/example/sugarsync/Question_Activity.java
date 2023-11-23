package com.example.sugarsync;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Question_Activity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private FirebaseUser mUser;
    private EditText username, password;
    private DatabaseReference userReference;
    private RadioGroup radioGroup;

    private RadioGroup radioGroup1, radioGroup2, radioGroup3, radioGroup4, radioGroup5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_activity);

        radioGroup1 = findViewById(R.id.optionsRadioGroup);
        radioGroup2 = findViewById(R.id.optionsRadioGroup2);
        radioGroup3 = findViewById(R.id.optionsRadioGroup3);
        radioGroup4 = findViewById(R.id.optionsRadioGroup4);
        radioGroup5 = findViewById(R.id.optionsRadioGroup5);

        fAuth = FirebaseAuth.getInstance();


        mUser = fAuth.getCurrentUser();

        if (mUser == null) {

            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        } else {
            String userID = mUser.getUid();
            userReference = FirebaseDatabase.getInstance().getReference("users").child(userID);
        }

        Button submitBtn = findViewById(R.id.submitBtn); // Replace with your button's ID
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int selectedOptionId = radioGroup1.getCheckedRadioButtonId();
                String selectedOption = getSelectedOption(selectedOptionId);

                int selectedOptionId2 = radioGroup2.getCheckedRadioButtonId();
                String selectedOption2 = getSelectedOption(selectedOptionId2);
                saveSelectedOption(selectedOption2);

                int selectedOptionId3 = radioGroup3.getCheckedRadioButtonId();
                String selectedOption3 = getSelectedOption(selectedOptionId3);
                saveSelectedOption(selectedOption3);

                int selectedOptionId4 = radioGroup4.getCheckedRadioButtonId();
                String selectedOption4 = getSelectedOption(selectedOptionId4);
                saveSelectedOption(selectedOption4);

                int selectedOptionId5 = radioGroup5.getCheckedRadioButtonId();
                String selectedOption5 = getSelectedOption(selectedOptionId5);
                saveSelectedOption(selectedOption5);

                saveSelectedOption(selectedOption);
                // Create an Intent to start the LoginActivity
                Intent intent = new Intent(Question_Activity.this, Home_Activity.class);
                startActivity(intent);
            }
        });


    }

    private String getSelectedOption(int selectedOptionId) {

        if (selectedOptionId == R.id.option1) {
            return "Option 1";
        } else if (selectedOptionId == R.id.option2) {
            return "Option 2";
        } else {
            return "No option selected";
        }
    }
    private void saveSelectedOption(String selectedOption) {
        // Save the selected option to the Realtime Database under the user's node
        userReference.child("selectedOption").setValue(selectedOption);
    }

}

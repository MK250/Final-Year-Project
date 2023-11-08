package com.example.sugarsync;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Question_Activity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private FirebaseUser mUser;
    private EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_activity);

        fAuth = FirebaseAuth.getInstance();


        mUser = fAuth.getCurrentUser();

        if (mUser == null) {

            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        } else {
            String userID = mUser.getUid();
        }

        Button submitBtn = findViewById(R.id.submitBtn); // Replace with your button's ID
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the LoginActivity
                Intent intent = new Intent(Question_Activity.this, Home_Activity.class);
                startActivity(intent);
            }
        });


    }

}

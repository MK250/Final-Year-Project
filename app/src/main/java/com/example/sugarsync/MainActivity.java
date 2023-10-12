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


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private FirebaseUser mUser;
    private EditText username, password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fAuth = FirebaseAuth.getInstance();

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);


        Button submit = findViewById(R.id.submit);
        submit.setOnClickListener(v -> registerUser());

        Button loginbtn = findViewById(R.id.loginbtn); // Replace with your button's ID
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, Login_Activity.class);
                startActivity(intent);
            }
        });

    }


        private void registerUser() {
            String usernameTextView = username.getText().toString();
            String passwordTextView = password.getText().toString();

            fAuth.createUserWithEmailAndPassword(usernameTextView, passwordTextView)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                mUser = fAuth.getCurrentUser();
                                String userID= mUser.getUid();

                                Toast.makeText(MainActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, Question_Activity.class);
                                startActivity(intent);
                            } else {

                                Toast.makeText(MainActivity.this, "Registration failed. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


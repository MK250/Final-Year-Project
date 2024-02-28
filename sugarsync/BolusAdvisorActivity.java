package com.example.sugarsync;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BolusAdvisorActivity extends AppCompatActivity {

    private EditText editTextGlucose, editTextCarbs, editTextISF;
    private Button buttonCalculate;
    private TextView textViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bolus_advisor);

        // Initialize views
        editTextGlucose = findViewById(R.id.editTextGlucose);
        editTextCarbs = findViewById(R.id.editTextCarbs);
        editTextISF = findViewById(R.id.editTextISF);
        buttonCalculate = findViewById(R.id.buttonCalculate);
        textViewResult = findViewById(R.id.textViewResult);

        // Set onClickListener for the calculate button
        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateBolus();
            }
        });
    }

    private void calculateBolus() {
        // Get inputs
        double glucoseLevel = Double.parseDouble(editTextGlucose.getText().toString());
        double carbIntake = Double.parseDouble(editTextCarbs.getText().toString());
        double isf = Double.parseDouble(editTextISF.getText().toString());

        // Calculate bolus
        double bolus = (glucoseLevel - 100) / isf + carbIntake / 10;

        // Display result
        textViewResult.setText(String.format("Recommended Bolus: %.2f units", bolus));
    }
}


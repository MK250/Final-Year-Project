package com.example.sugarsync;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.Arrays;
import java.util.List;

public class BolusAdvisorActivity extends AppCompatActivity {

    private LinearLayout youtubePlayerContainer;

    private EditText editTextGlucose, editTextCarbs, editTextISF;
    private Button buttonCalculate;
    private TextView textViewResult;

    private List<String> playlist = Arrays.asList(
            "povArl9xZSg");

    private int currentVideoIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bolus_advisor);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.customActionBarColor)));
        }

        // Initialize views
        editTextGlucose = findViewById(R.id.editTextGlucose);
        editTextCarbs = findViewById(R.id.editTextCarbs);
        editTextISF = findViewById(R.id.editTextISF);
        buttonCalculate = findViewById(R.id.buttonCalculate);
        textViewResult = findViewById(R.id.textViewResult);
        youtubePlayerContainer = findViewById(R.id.youtube_player_view);

        refreshYouTubeVideo();

        // Set onClickListener for the calculate button
        buttonCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateBolus();
            }
        });
    }

    private void refreshYouTubeVideo() {
        YouTubePlayerView youTubePlayerView = new YouTubePlayerView(this); // or use getContext() instead of this
        youtubePlayerContainer.removeAllViews();
        youtubePlayerContainer.addView(youTubePlayerView);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                youTubePlayer.loadVideo(playlist.get(currentVideoIndex), 0);
                currentVideoIndex = (currentVideoIndex + 1) % playlist.size();
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


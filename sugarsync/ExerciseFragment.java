package com.example.sugarsync;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExerciseFragment extends Fragment {

    private LinearLayout youtubePlayerContainer;
    private Button buttonSubmit;

    private Button buttonNext;


    //private YouTubePlayerView youTubePlayerView;
    private EditText editTextExerciseType, editTextExerciseTime;


    private List<String> playlist = Arrays.asList(
            "ePylP2XmNRs",
            "WDIGXWZhC4M",
            "EsVLl_bEcXw" );


    private int currentVideoIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        editTextExerciseType = view.findViewById(R.id.editTextExerciseType);
        editTextExerciseTime = view.findViewById(R.id.editTextExerciseTime);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);
        buttonNext = view.findViewById(R.id.buttonNext);

        youtubePlayerContainer = view.findViewById(R.id.youtube_player_view);
       // youTubePlayerView = new YouTubePlayerView(requireContext());
        buttonSubmit = view.findViewById(R.id.buttonSubmit);

        // Set a click listener for the Submit button
        //buttonSubmit.setOnClickListener(v -> refreshYouTubeVideo());
        buttonSubmit.setOnClickListener(v -> saveExerciseData());

        buttonNext.setOnClickListener(v -> refreshYouTubeVideo());
        refreshYouTubeVideo(); // Load the initial video

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Ensure that the fragment has options menu
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.exercise_menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_view_all_exercises) {
            // Handle the "View All Exercises" action
            Intent intent = new Intent(requireContext(), ViewAllExerciseActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void saveExerciseData() {
        // Get user ID from Firebase authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not logged in, handle accordingly
            return;
        }

        String userId = currentUser.getUid();

        // Get the exercise data from EditText fields
        String exerciseType = editTextExerciseType.getText().toString();
        double exerciseTime = Double.parseDouble(editTextExerciseTime.getText().toString());

        // Get the current date
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Create a reference to the "users" node in Firebase
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Create a child node under "users" with the user ID
        DatabaseReference userExercisesRef = usersRef.child(userId).child("exercise");

        // Generate a unique key for each exercise entry
        String exerciseId = userExercisesRef.push().getKey();

        // Create a child node under "exercise" with the generated key
        DatabaseReference exerciseRef = userExercisesRef.child(exerciseId);

        // Set values for the "exercise" node
        exerciseRef.child("date").setValue(date); // Set the date field
        exerciseRef.child("exerciseType").setValue(exerciseType);
        exerciseRef.child("exerciseTime").setValue(exerciseTime);

        // Clear the input fields
        editTextExerciseType.setText("");
        editTextExerciseTime.setText("");

        // Display a toast message
        Toast.makeText(requireContext(), "Exercise is saved", Toast.LENGTH_SHORT).show();
    }

    private void refreshYouTubeVideo() {
        // Refresh the YouTube video by re-initializing the player
        YouTubePlayerView youTubePlayerView = new YouTubePlayerView(requireContext());
        youtubePlayerContainer.removeAllViews();
        youtubePlayerContainer.addView(youTubePlayerView);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                // Load the video from the playlist based on the current index
                youTubePlayer.loadVideo(playlist.get(currentVideoIndex), 0);

                // Increment the index for the next video
                currentVideoIndex = (currentVideoIndex + 1) % playlist.size();

                // Optional: Implement logic to play the next video when the current one ends

            }
        });
    }
}


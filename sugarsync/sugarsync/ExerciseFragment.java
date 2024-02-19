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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private EditText editTextExerciseType, editTextExerciseTime;
    private ProgressBar progressBar;
    private String lastUpdatedDate;

    private TextView textProgressFraction;

    private List<String> playlist = Arrays.asList(
            "ePylP2XmNRs",
            "WDIGXWZhC4M",
            "EsVLl_bEcXw");

    private int currentVideoIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        editTextExerciseType = view.findViewById(R.id.editTextExerciseType);
        editTextExerciseTime = view.findViewById(R.id.editTextExerciseTime);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);
        buttonNext = view.findViewById(R.id.buttonNext);
        youtubePlayerContainer = view.findViewById(R.id.youtube_player_view);
        progressBar = view.findViewById(R.id.progressBar);
        textProgressFraction = view.findViewById(R.id.textProgressFraction);

        buttonSubmit.setOnClickListener(v -> saveExerciseData());
        buttonNext.setOnClickListener(v -> refreshYouTubeVideo());
        refreshYouTubeVideo(); // Load the initial video

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Call resetProgressBarIfNeeded only when the fragment is resumed
        resetProgressBarIfNeeded();
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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String userId = currentUser.getUid();
        String exerciseType = editTextExerciseType.getText().toString();
        double exerciseTime = Double.parseDouble(editTextExerciseTime.getText().toString());

        updateProgressBar(exerciseTime);

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        lastUpdatedDate = date;
        saveDateToFirebase(date);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference userExercisesRef = usersRef.child(userId).child("exercise");
        String exerciseId = userExercisesRef.push().getKey();
        DatabaseReference exerciseRef = userExercisesRef.child(exerciseId);
        exerciseRef.child("date").setValue(date);
        exerciseRef.child("exerciseType").setValue(exerciseType);
        exerciseRef.child("exerciseTime").setValue(exerciseTime);

        editTextExerciseType.setText("");
        editTextExerciseTime.setText("");

        Toast.makeText(requireContext(), "Exercise is saved", Toast.LENGTH_SHORT).show();
    }

    private void updateProgressBar(double exerciseTime) {
        int progress = (int) (progressBar.getProgress() + exerciseTime);
        if (progress > 130) {
            progressBar.setProgress(130);
            progress = 130;
        } else {
            progressBar.setProgress(progress);
        }

        String progressText = progress + "/130";
        textProgressFraction.setText(progressText);
    }

    private void resetProgressBarIfNeeded() {
        DatabaseReference lastUpdatedDateRef = FirebaseDatabase.getInstance().getReference("lastUpdatedDate");
        lastUpdatedDateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String lastUpdatedDate = dataSnapshot.getValue(String.class);
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                if (lastUpdatedDate == null || !currentDate.equals(lastUpdatedDate)) {
                    progressBar.setProgress(0);
                    lastUpdatedDateRef.setValue(currentDate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors here
            }
        });
    }

    private void saveDateToFirebase(String date) {
        DatabaseReference lastUpdatedDateRef = FirebaseDatabase.getInstance().getReference("lastUpdatedDate");
        lastUpdatedDateRef.setValue(date);
    }


    private void refreshYouTubeVideo() {
        YouTubePlayerView youTubePlayerView = new YouTubePlayerView(requireContext());
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
}
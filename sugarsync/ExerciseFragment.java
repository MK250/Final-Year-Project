package com.example.sugarsync;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.Arrays;
import java.util.List;

public class ExerciseFragment extends Fragment {

    private LinearLayout youtubePlayerContainer;
    private Button buttonSubmit;




    private List<String> playlist = Arrays.asList(
            "ePylP2XmNRs",
            "WDIGXWZhC4M",
            "EsVLl_bEcXw" );


    private int currentVideoIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        youtubePlayerContainer = view.findViewById(R.id.youtube_player_view);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);

        // Set a click listener for the Submit button
        buttonSubmit.setOnClickListener(v -> refreshYouTubeVideo());

        refreshYouTubeVideo(); // Load the initial video

        return view;
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



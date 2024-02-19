package com.example.sugarsync;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.common.util.concurrent.ListenableFuture;
//import com.googlecode.tesseract.android.TessBaseAPI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class Home_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        // Load HomeFragment when the activity is created
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (item.getItemId() == R.id.navigation_home) {
                // If the selected item is "Home," always replace with HomeFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            } else if (item.getItemId() == R.id.navigation_glucose && !(currentFragment instanceof GlucoseFragment)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new GlucoseFragment())
                        .commit();
            } else if (item.getItemId() == R.id.navigation_diet && !(currentFragment instanceof DietFragment)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new DietFragment())
                        .commit();
            } else if (item.getItemId() == R.id.navigation_exercise && !(currentFragment instanceof ExerciseFragment)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ExerciseFragment())
                        .commit();
            } else if (item.getItemId() == R.id.navigation_profile && !(currentFragment instanceof ProfileFragment)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .commit();
            }

            return true;
        });
    }
}

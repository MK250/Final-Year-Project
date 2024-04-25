package com.example.sugarsync;



import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.logging.HttpLoggingInterceptor;



public class DietFragment extends Fragment {

    //private DietAdapter dietAdapter;

    private EditText editTextBreakfast, editTextLunch, editTextDinner;
    private Button buttonSubmit, btnGetNutritionalInfo;

    //private RecyclerView recyclerView;

    private View view;

    private ProgressBar progressBarSugar;

    private TextView textProgressFraction;

    private EdamamApiService edamamApiService;

    public DietFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_diet, container, false);



        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.customActionBarColor)));
        }

        // Initialize UI components
        editTextBreakfast = view.findViewById(R.id.editTextBreakfast);
        editTextLunch = view.findViewById(R.id.editTextLunch);
        editTextDinner = view.findViewById(R.id.editTextDinner);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);
        //recyclerView = view.findViewById(R.id.recyclerView);
        progressBarSugar = view.findViewById(R.id.progressBarSugar);
        textProgressFraction = view.findViewById(R.id.textProgressFraction);

        //RecyclerView recyclerView = view.findViewById(R.id.recyclerView);



        // Set click listener for the Submit button
        buttonSubmit.setOnClickListener(v -> saveDietData());

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.edamam.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();


        // Create an instance of the EdamamApiService
        edamamApiService = retrofit.create(EdamamApiService.class);

        // Set click listener for the Get Nutritional Info button
        btnGetNutritionalInfo = view.findViewById(R.id.btnGetNutritionalInfo);
        btnGetNutritionalInfo.setOnClickListener(v -> getNutritionalInfo());


        setupAlarmManager();

        checkDietForPast24Hours();
        return view;


    }







    private void checkDietForPast24Hours() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {

            return;
        }

        String userId = currentUser.getUid();

        // Get the current date and the date 24 hours ago
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date twentyFourHoursAgo = calendar.getTime();

        // Convert dates to formatted strings
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        String twentyFourHoursAgoDate = dateFormat.format(twentyFourHoursAgo);

        // Debugging statements
        Log.d("Date_Debug", "Current Date: " + currentDate);
        Log.d("Date_Debug", "24 Hours Ago: " + twentyFourHoursAgoDate);

        // Query Firebase to check if there is diet data for the past 24 hours
        DatabaseReference userDietsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("diets");

        userDietsRef.orderByChild("date")
                .startAt(twentyFourHoursAgoDate)
                .endAt(currentDate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            // No diet data found for the past 24 hours, show alert dialogue
                            Log.d("Diet_Debug", "No diet data found for the past 24 hours");
                            showNoDietDataAlert();
                        } else {
                            Log.d("Diet_Debug", "Diet data found for the past 24 hours");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    private void showNoDietDataAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Reminder");
        builder.setMessage("You haven't inputted your diet for the past 24 hours. Please make sure to record your meals.");

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void getNutritionalInfo() {
        // Get the diet data from EditText fields
        String breakfast = editTextBreakfast.getText().toString();
        String lunch = editTextLunch.getText().toString();
        String dinner = editTextDinner.getText().toString();

        // Concatenate the input for all meals
        String ingredients = breakfast + "," + lunch + "," + dinner;

        // Make an API request to Edamam
        Call<NutritionResponse> call = edamamApiService.getNutritionalInfo(
                "49a44627", // app_id
                "d970edb6ec195faa3fe67460ed7b1010", // app_key
                ingredients
        );


        call.enqueue(new Callback<NutritionResponse>() {
            @Override
            public void onResponse(Call<NutritionResponse> call, Response<NutritionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Display the nutritional information in an AlertDialog
                    showNutritionalInfo(response.body());
                } else {
                    // Log the error response
                    Log.e("API_RESPONSE_ERROR", response.toString());

                    // Handle API error
                    Toast.makeText(requireContext(), "Error fetching nutritional information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NutritionResponse> call, Throwable t) {
                // Log the failure details
                Log.e("API_REQUEST_FAILURE", "Failed to make API request", t);

                // Handle network or request failure
                Toast.makeText(requireContext(), "Failed to fetch nutritional information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNutritionalInfo(NutritionResponse nutritionResponse) {
        // Build and display an AlertDialog with the nutritional information
        Log.d("NutritionResponse", nutritionResponse.toString());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Nutritional Information");

        // Get the nutritional details from the NutritionResponse
        double calories = nutritionResponse.getCalories();
        NutritionResponse.TotalNutrients totalNutrients = nutritionResponse.getTotalNutrients();

        // Customize the AlertDialog content based on the nutritionResponse object
        StringBuilder nutritionalInfo = new StringBuilder();
        nutritionalInfo.append("Calories: ").append(calories).append(" kcal\n");

        // Add other nutritional information
        if (totalNutrients != null) {
            nutritionalInfo.append("Protein: ").append(getNutrientDetails(totalNutrients.getProtein())).append("\n");
            nutritionalInfo.append("Carbs: ").append(getNutrientDetails(totalNutrients.getCarbs())).append("\n");
            nutritionalInfo.append("Fat: ").append(getNutrientDetails(totalNutrients.getFat())).append("\n");
            nutritionalInfo.append("Sugar: ").append(getNutrientDetails(totalNutrients.getSugar())).append("\n");




        }

        // Set the nutritional information in the AlertDialog
        builder.setMessage(nutritionalInfo.toString());

        // Set a positive button to close the dialog
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        // Show the AlertDialog
        builder.create().show();
    }



    private String getNutrientDetails(NutritionResponse.Nutrient nutrient) {
        if (nutrient != null) {
            // Log the nutrient details
            Log.d("NutrientDetails", "Label: " + nutrient.getLabel() + ", Quantity: " + nutrient.getQuantity() + ", Unit: " + nutrient.getUnit());

            // Check if the quantity is greater than zero
            if (nutrient.getQuantity() > 0) {
                return nutrient.getLabel() + ": " + nutrient.getQuantity() + " " + nutrient.getUnit();
            }
        }
        return "N/A";
    }





    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Ensure that the fragment has options menu
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.diet_menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_view_all_diets) {
            // Handle the "View All Diets" action
            Intent intent = new Intent(requireContext(), ViewAllDietsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }




    private void saveDietData() {
        // Get user ID from Firebase authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not logged in, handle accordingly
            return;
        }

        String userId = currentUser.getUid();

        // Get the current date
        String currentDate = getCurrentDate();

        // Get the diet data from EditText fields
        String breakfast = editTextBreakfast.getText().toString();
        String lunch = editTextLunch.getText().toString();
        String dinner = editTextDinner.getText().toString();



        // Make an API request to get nutritional info for all meals
        String ingredients = breakfast + "," + lunch + "," + dinner;
        Call<NutritionResponse> call = edamamApiService.getNutritionalInfo(
                "49a44627", // app_id
                "d970edb6ec195faa3fe67460ed7b1010", // replace with your app key
                ingredients
        );

        call.enqueue(new Callback<NutritionResponse>() {
            @Override
            public void onResponse(Call<NutritionResponse> call, Response<NutritionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Get sugar content
                    double sugar = response.body().getTotalNutrients().getSugar().getQuantity();
                    // Save the diet data to Firebase along with sugar content
                    saveDietDataToFirebase(userId, currentDate, breakfast, lunch, dinner, sugar);
                } else {
                    // Log the error response
                    Log.e("API_RESPONSE_ERROR", response.toString());

                    // Handle API error
                    Toast.makeText(requireContext(), "Error fetching nutritional information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NutritionResponse> call, Throwable t) {
                // Log the failure details
                Log.e("API_REQUEST_FAILURE", "Failed to make API request", t);

                // Handle network or request failure
                Toast.makeText(requireContext(), "Failed to fetch nutritional information", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void saveDietDataToFirebase(String userId, String currentDate, String breakfast, String lunch, String dinner, double sugar) {
        // Create a reference to the "users" node in Firebase
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Create a child node under "users" with the user ID
        DatabaseReference userDietsRef = usersRef.child(userId).child("diets");

        // Generate a unique key for each diet entry
        String dietId = userDietsRef.push().getKey();

        // Create a child node under "diets" with the generated key
        DatabaseReference dietRef = userDietsRef.child(dietId);

        // Set values for the "diet" node
        dietRef.child("date").setValue(currentDate);
        dietRef.child("breakfast").setValue(breakfast);
        dietRef.child("lunch").setValue(lunch);
        dietRef.child("dinner").setValue(dinner);
        dietRef.child("sugar").setValue(sugar);

        // Clear the input fields
        editTextBreakfast.setText("");
        editTextLunch.setText("");
        editTextDinner.setText("");

        // Display a toast message
        Toast.makeText(requireContext(), "Diet is saved", Toast.LENGTH_SHORT).show();




        // Update the progress bar to reflect the sugar intake
        progressBarSugar.setProgress((int) sugar);
        textProgressFraction.setText(sugar + "/30g");


    }





    // Method to get the current date in a formatted string
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void setupAlarmManager() {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent(requireContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set the alarm to trigger every day at 1 PM
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 13); // 1 PM
        calendar.set(Calendar.MINUTE, 0);
        long triggerTime = calendar.getTimeInMillis();

        // Ensure the trigger time is in the future
        if (triggerTime <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1); // Move to next day if the trigger time has passed for today
            triggerTime = calendar.getTimeInMillis();
        }

        // Set interval to repeat the alarm daily
        long intervalMillis = AlarmManager.INTERVAL_DAY;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, intervalMillis, pendingIntent);
    }


    private void triggerImmediateNotification() {
        Intent intent = new Intent(requireContext(), NotificationReceiver.class);
        intent.setAction("TEST_NOTIFICATION_ACTION"); // Custom action to identify immediate testing
        requireContext().sendBroadcast(intent);
    }




}


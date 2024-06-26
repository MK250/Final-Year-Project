package com.example.sugarsync;

import android.text.TextUtils;

import java.util.UUID;

public class Exercise {

    private String id;
    private String date;
    private String exerciseType;
    private double exerciseTime;

    // Default constructor (required for Firebase)
    public Exercise() {
        //this.id = UUID.randomUUID().toString();
    }

    // Constructor to initialize fields
    public Exercise(String date, String exerciseType, double exerciseTime) {
        // Only generate a new ID if it's not set
        if (TextUtils.isEmpty(id)) {
            this.id = UUID.randomUUID().toString();
        }
        this.date = date;
        this.exerciseType = exerciseType;
        this.exerciseTime = exerciseTime;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter methods
    public String getDate() {
        return date;
    }

    public String getExerciseType() {
        return exerciseType;
    }

    public double getExerciseTime() {
        return exerciseTime;
    }

    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }

    public void setExerciseTime(double exerciseTime) {
        this.exerciseTime = exerciseTime;
    }
}

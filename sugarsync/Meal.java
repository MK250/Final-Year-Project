package com.example.sugarsync;

public class Meal {
    private String time;
    private String content;

    // Default constructor required for Firebase
    public Meal() {
    }

    // Constructor to initialize fields
    public Meal(String time, String content) {
        this.time = time;
        this.content = content;
    }

    // Getter and setter methods
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // Additional methods if needed
}


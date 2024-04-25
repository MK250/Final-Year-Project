package com.example.sugarsync;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


public class Diet {

    private String id;
    private String date;



    private String breakfast;
    private String lunch;
    private String dinner;


    public Diet() {
        this.id = UUID.randomUUID().toString();
    }


    // Constructor to initialize fields
    public Diet(String id, String date, String breakfast, String lunch, String dinner) {
        this.id = id;
        this.date = date;
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
    }

    // Getter methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // Setter methods
    public String getBreakfast() {
        return breakfast;
    }

    public void setBreakfast(String breakfast) {
        this.breakfast = breakfast;
    }

    public String getLunch() {
        return lunch;
    }

    public void setLunch(String lunch) {
        this.lunch = lunch;
    }

    public String getDinner() {
        return dinner;
    }

    public void setDinner(String dinner) {
        this.dinner = dinner;
    }

    public void removeMealType(String mealType) {
        switch (mealType) {
            case "breakfast":
                setBreakfast(null);
                break;
            case "lunch":
                setLunch(null);
                break;
            case "dinner":
                setDinner(null);
                break;
            // Add more cases if you have other meal types
        }
    }

    public boolean isMealTypeNull(String mealType) {
        switch (mealType) {
            case "breakfast":
                return getBreakfast() == null;
            case "lunch":
                return getLunch() == null;
            case "dinner":
                return getDinner() == null;
            default:
                return true; // Handle additional meal types if needed
        }
    }

}
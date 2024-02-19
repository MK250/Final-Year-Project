package com.example.sugarsync;

import com.google.gson.annotations.SerializedName;

public class NutritionResponse {

    @SerializedName("calories")
    private double calories;

    @SerializedName("totalNutrients")
    private TotalNutrients totalNutrients;

    public NutritionResponse(double calories, TotalNutrients totalNutrients) {
        this.calories = calories;
        this.totalNutrients = totalNutrients;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public TotalNutrients getTotalNutrients() {
        return totalNutrients;
    }

    public void setTotalNutrients(TotalNutrients totalNutrients) {
        this.totalNutrients = totalNutrients;
    }

    public static class TotalNutrients {

        @SerializedName("PROCNT")
        private Nutrient protein;

        @SerializedName("CHOCDF.net")
        private Nutrient carbs;

        @SerializedName("FAT")
        private Nutrient fat;

        @SerializedName("SUGAR")
        private Nutrient sugar;

        public Nutrient getProtein() {
            return protein;
        }

        public void setProtein(Nutrient protein) {
            this.protein = protein;
        }

        public Nutrient getCarbs() {
            return carbs;
        }

        public void setCarbs(Nutrient carbs) {
            this.carbs = carbs;
        }

        public Nutrient getFat() {
            return fat;
        }

        public void setFat(Nutrient fat) {
            this.fat = fat;
        }

        public Nutrient getSugar() {
            return sugar;
        }

        public void setSugar(Nutrient sugar) {
            this.sugar = sugar;
        }
    }

    public static class Nutrient {

        @SerializedName("label")
        private String label;

        @SerializedName("quantity")
        private double quantity;

        @SerializedName("unit")
        private String unit;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public double getQuantity() {
            return quantity;
        }

        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
}

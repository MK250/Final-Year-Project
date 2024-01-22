package com.example.sugarsync;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DietAdapter extends RecyclerView.Adapter<DietAdapter.DietViewHolder> {

    private List<Diet> dietList;
    private OnEditClickListener onEditClickListener; // Remove static keyword
    private OnDeleteClickListener onDeleteClickListener;



    // Constructor
    public DietAdapter() {
        this.dietList = new ArrayList<>();
    }

    // ViewHolder class
    public static class DietViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, breakfastTextView, lunchTextView, dinnerTextView;
        ImageView imageViewEditBreakfast, imageViewDeleteBreakfast,
                imageViewEditLunch, imageViewDeleteLunch,
                imageViewEditDinner, imageViewDeleteDinner;

        public DietViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            breakfastTextView = itemView.findViewById(R.id.breakfastTextView);
            lunchTextView = itemView.findViewById(R.id.lunchTextView);
            dinnerTextView = itemView.findViewById(R.id.dinnerTextView);
            imageViewEditBreakfast = itemView.findViewById(R.id.imageViewEditBreakfast);
            imageViewDeleteBreakfast = itemView.findViewById(R.id.imageViewDeleteBreakfast);
            imageViewEditLunch = itemView.findViewById(R.id.imageViewEditLunch);
            imageViewDeleteLunch = itemView.findViewById(R.id.imageViewDeleteLunch);
            imageViewEditDinner = itemView.findViewById(R.id.imageViewEditDinner);
            imageViewDeleteDinner = itemView.findViewById(R.id.imageViewDeleteDinner);
        }
    }

    // Add a diet to the adapter
    public void addDiet(Diet diet) {
        dietList.add(diet);
        notifyItemInserted(dietList.size() - 1);
    }

    // Clear all diets from the adapter
    public void clearDiets() {
        dietList.clear();
        notifyDataSetChanged();
    }

    public void removeDiet(int position) {
        dietList.remove(position);
        notifyItemRemoved(position);
    }

    public String getDietId(int position) {
        if (dietList != null && position >= 0 && position < dietList.size()) {
            return dietList.get(position).getId();
        }
        return null;
    }

    @NonNull
    @Override
    public DietViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.diet_item_layout, parent, false);
        return new DietViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DietViewHolder holder, int position) {
        Diet diet = dietList.get(position);
        holder.dateTextView.setText("Date: " + diet.getDate());
        holder.breakfastTextView.setText("Breakfast: " + diet.getBreakfast());
        holder.lunchTextView.setText("Lunch: " + diet.getLunch());
        holder.dinnerTextView.setText("Dinner: " + diet.getDinner());

        if (diet.isMealTypeNull("breakfast")) {
            holder.breakfastTextView.setVisibility(View.GONE);
        } else {
            holder.breakfastTextView.setVisibility(View.VISIBLE);
        }


        holder.imageViewEditBreakfast.setOnClickListener(v -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(position, diet, "breakfast");
            }
        });

        holder.imageViewDeleteBreakfast.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(position, diet, "breakfast");
            }
        });

        holder.imageViewEditLunch.setOnClickListener(v -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(position, diet, "lunch");
            }
        });


        holder.imageViewDeleteLunch.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(position, diet, "lunch");
            }
        });

        holder.imageViewEditDinner.setOnClickListener(v -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(position, diet, "dinner");
            }
        });

        holder.imageViewDeleteDinner.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(position, diet, "dinner");
            }
        });


        // Check if the meal is empty and hide the corresponding views
        if (TextUtils.isEmpty(diet.getBreakfast())) {
            holder.breakfastTextView.setVisibility(View.GONE);
            holder.imageViewEditBreakfast.setVisibility(View.GONE);
            holder.imageViewDeleteBreakfast.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(diet.getLunch())) {
            holder.lunchTextView.setVisibility(View.GONE);
            holder.imageViewEditLunch.setVisibility(View.GONE);
            holder.imageViewDeleteLunch.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(diet.getDinner())) {
            holder.dinnerTextView.setVisibility(View.GONE);
            holder.imageViewEditDinner.setVisibility(View.GONE);
            holder.imageViewDeleteDinner.setVisibility(View.GONE);
        }
    }

    public void removeMealType(int position, String mealType) {
        Diet diet = dietList.get(position);
        diet.removeMealType(mealType);
        notifyItemChanged(position);
    }






    public interface OnEditClickListener {
        void onEditClick(int position, Diet diet, String mealType);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position, Diet diet, String mealType);
    }
    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickListener = listener;
    }

    // Setter method for the delete click listener
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }


    @Override
    public int getItemCount() {
        return dietList.size();
    }
}

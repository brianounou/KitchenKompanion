package com.kitchenkompanion.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response from Spoonacular recipe search API.
 */
public class RecipeSearchResponse {
    
    @SerializedName("id")
    public int id;
    
    @SerializedName("title")
    public String title;
    
    @SerializedName("image")
    public String image;
    
    @SerializedName("imageType")
    public String imageType;
    
    // For findByIngredients
    @SerializedName("usedIngredients")
    public List<Ingredient> usedIngredients;
    
    @SerializedName("missedIngredients")
    public List<Ingredient> missedIngredients;
    
    @SerializedName("unusedIngredients")
    public List<Ingredient> unusedIngredients;
    
    @SerializedName("usedIngredientCount")
    public int usedIngredientCount;
    
    @SerializedName("missedIngredientCount")
    public int missedIngredientCount;
    
    // For complexSearch with recipe information
    @SerializedName("readyInMinutes")
    public int readyInMinutes;
    
    @SerializedName("servings")
    public int servings;
    
    public static class Ingredient {
        @SerializedName("id")
        public int id;
        
        @SerializedName("name")
        public String name;
        
        @SerializedName("amount")
        public double amount;
        
        @SerializedName("unit")
        public String unit;
        
        @SerializedName("image")
        public String image;
    }
}







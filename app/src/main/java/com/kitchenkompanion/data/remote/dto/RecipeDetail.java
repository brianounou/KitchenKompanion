package com.kitchenkompanion.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Detailed recipe information from Spoonacular API.
 */
public class RecipeDetail {
    
    @SerializedName("id")
    public int id;
    
    @SerializedName("title")
    public String title;
    
    @SerializedName("image")
    public String image;
    
    @SerializedName("servings")
    public int servings;
    
    @SerializedName("readyInMinutes")
    public int readyInMinutes;
    
    @SerializedName("sourceUrl")
    public String sourceUrl;
    
    @SerializedName("summary")
    public String summary;
    
    @SerializedName("cuisines")
    public List<String> cuisines;
    
    @SerializedName("dishTypes")
    public List<String> dishTypes;
    
    @SerializedName("extendedIngredients")
    public List<ExtendedIngredient> extendedIngredients;
    
    @SerializedName("analyzedInstructions")
    public List<AnalyzedInstruction> analyzedInstructions;
    
    @SerializedName("nutrition")
    public Nutrition nutrition;
    
    public static class ExtendedIngredient {
        @SerializedName("id")
        public int id;
        
        @SerializedName("name")
        public String name;
        
        @SerializedName("original")
        public String original;
        
        @SerializedName("amount")
        public double amount;
        
        @SerializedName("unit")
        public String unit;
        
        @SerializedName("image")
        public String image;
    }
    
    public static class AnalyzedInstruction {
        @SerializedName("name")
        public String name;
        
        @SerializedName("steps")
        public List<Step> steps;
    }
    
    public static class Step {
        @SerializedName("number")
        public int number;
        
        @SerializedName("step")
        public String step;
    }
    
    public static class Nutrition {
        @SerializedName("nutrients")
        public List<Nutrient> nutrients;
    }
    
    public static class Nutrient {
        @SerializedName("name")
        public String name;
        
        @SerializedName("amount")
        public double amount;
        
        @SerializedName("unit")
        public String unit;
    }
}







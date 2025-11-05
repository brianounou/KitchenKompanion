package com.kitchenkompanion.data.remote;

import com.kitchenkompanion.data.remote.dto.RecipeSearchResponse;
import com.kitchenkompanion.data.remote.dto.RecipeDetail;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit API for Spoonacular Recipe API.
 * 
 * Free tier: 150 requests/day
 * Documentation: https://spoonacular.com/food-api/docs
 * 
 * API Key should be set in secrets.properties:
 * SPOONACULAR_API_KEY=your_key_here
 */
public interface SpoonacularApi {
    
    /**
     * Search recipes by ingredients.
     * 
     * @param ingredients Comma-separated list of ingredients (e.g., "apples,flour,sugar")
     * @param number Number of results (default: 10)
     * @param ranking Maximize used ingredients (1) or minimize missing ingredients (2)
     * @param apiKey Your API key
     */
    @GET("recipes/findByIngredients")
    Call<List<RecipeSearchResponse>> findByIngredients(
            @Query("ingredients") String ingredients,
            @Query("number") int number,
            @Query("ranking") int ranking,
            @Query("apiKey") String apiKey
    );
    
    /**
     * Get detailed recipe information.
     * 
     * @param id Recipe ID
     * @param apiKey Your API key
     */
    @GET("recipes/{id}/information")
    Call<RecipeDetail> getRecipeDetail(
            @Path("id") int id,
            @Query("apiKey") String apiKey
    );
    
    /**
     * Search recipes by query (name, cuisine, etc.)
     * 
     * @param query Search query
     * @param number Number of results
     * @param apiKey Your API key
     */
    @GET("recipes/complexSearch")
    Call<RecipeSearchResponse> searchRecipes(
            @Query("query") String query,
            @Query("number") int number,
            @Query("addRecipeInformation") boolean addInfo,
            @Query("apiKey") String apiKey
    );
}







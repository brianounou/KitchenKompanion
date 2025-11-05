package com.kitchenkompanion.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * Room entity for caching recipe data from external APIs.
 */
@Entity(tableName = "recipe_cache")
public class RecipeCacheEntity {
    
    @PrimaryKey
    @ColumnInfo(name = "id")
    public String id;
    
    @ColumnInfo(name = "title")
    public String title;
    
    @ColumnInfo(name = "image_url")
    public String imageUrl;
    
    @ColumnInfo(name = "source")
    public String source; // spoonacular, edamam, etc.
    
    @ColumnInfo(name = "recipe_json")
    public String recipeJson; // Full recipe data as JSON
    
    @ColumnInfo(name = "servings")
    public int servings;
    
    @ColumnInfo(name = "ready_in_minutes")
    public int readyInMinutes;
    
    @ColumnInfo(name = "cached_at")
    public Date cachedAt;
    
    public RecipeCacheEntity() {
    }
}


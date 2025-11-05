package com.kitchenkompanion.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.Date;
import java.util.List;

/**
 * DAO for recipe cache.
 */
@Dao
public interface RecipeCacheDao {
    
    @Query("SELECT * FROM recipe_cache WHERE id = :recipeId")
    RecipeCacheEntity getRecipeById(String recipeId);
    
    @Query("SELECT * FROM recipe_cache ORDER BY cached_at DESC LIMIT :limit")
    List<RecipeCacheEntity> getRecentRecipes(int limit);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RecipeCacheEntity recipe);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RecipeCacheEntity> recipes);
    
    @Query("DELETE FROM recipe_cache WHERE cached_at < :expiryDate")
    void deleteOldCache(Date expiryDate);
    
    @Query("DELETE FROM recipe_cache")
    void clearCache();
}








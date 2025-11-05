package com.kitchenkompanion.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Room database for Kitchen Kompanion.
 * Version 1: Initial schema with items, grocery entries, households, and recipe cache.
 */
@Database(
    entities = {
        ItemEntity.class,
        GroceryEntryEntity.class,
        HouseholdEntity.class,
        RecipeCacheEntity.class
    },
    version = 1,
    exportSchema = true
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "kitchen_kompanion_db";
    private static volatile AppDatabase INSTANCE;
    
    public abstract ItemDao itemDao();
    public abstract GroceryDao groceryDao();
    public abstract HouseholdDao householdDao();
    public abstract RecipeCacheDao recipeCacheDao();
    
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration() // For development; use proper migrations in production
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}








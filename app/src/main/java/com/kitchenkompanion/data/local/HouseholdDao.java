package com.kitchenkompanion.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * DAO for household data.
 */
@Dao
public interface HouseholdDao {
    
    @Query("SELECT * FROM households")
    LiveData<List<HouseholdEntity>> getAllHouseholds();
    
    @Query("SELECT * FROM households")
    List<HouseholdEntity> getAllHouseholdsSync();
    
    @Query("SELECT * FROM households WHERE id = :householdId")
    LiveData<HouseholdEntity> getHouseholdById(String householdId);
    
    @Query("SELECT * FROM households WHERE id = :householdId")
    HouseholdEntity getHouseholdByIdSync(String householdId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HouseholdEntity household);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<HouseholdEntity> households);
    
    @Update
    void update(HouseholdEntity household);
    
    @Query("DELETE FROM households WHERE id = :householdId")
    void delete(String householdId);
    
    @Query("DELETE FROM households")
    void deleteAll();
}








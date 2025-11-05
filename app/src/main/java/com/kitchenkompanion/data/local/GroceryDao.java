package com.kitchenkompanion.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

/**
 * DAO for grocery list entries.
 */
@Dao
public interface GroceryDao {
    
    @Query("SELECT * FROM grocery_entries WHERE household_id = :householdId AND is_deleted = 0 ORDER BY is_checked ASC, created_at DESC")
    LiveData<List<GroceryEntryEntity>> getAllEntries(String householdId);
    
    @Query("SELECT * FROM grocery_entries WHERE household_id = :householdId AND is_deleted = 0 ORDER BY is_checked ASC, created_at DESC")
    List<GroceryEntryEntity> getAllEntriesSync(String householdId);
    
    @Query("SELECT * FROM grocery_entries WHERE id = :entryId")
    GroceryEntryEntity getEntryByIdSync(String entryId);
    
    @Query("SELECT * FROM grocery_entries WHERE household_id = :householdId AND name = :name AND is_deleted = 0 LIMIT 1")
    GroceryEntryEntity findByNameSync(String householdId, String name);
    
    @Query("SELECT * FROM grocery_entries WHERE is_synced = 0")
    List<GroceryEntryEntity> getUnsyncedEntries();
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GroceryEntryEntity entry);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GroceryEntryEntity> entries);
    
    @Update
    void update(GroceryEntryEntity entry);
    
    @Delete
    void delete(GroceryEntryEntity entry);
    
    @Query("UPDATE grocery_entries SET is_deleted = 1, updated_at = :timestamp WHERE id = :entryId")
    void softDelete(String entryId, Date timestamp);
    
    @Query("UPDATE grocery_entries SET is_checked = :checked, updated_at = :timestamp, is_synced = 0 WHERE id = :entryId")
    void updateCheckedStatus(String entryId, boolean checked, Date timestamp);
    
    @Query("UPDATE grocery_entries SET is_synced = 1 WHERE id = :entryId")
    void markAsSynced(String entryId);
    
    @Query("DELETE FROM grocery_entries WHERE is_checked = 1 AND household_id = :householdId")
    void deleteCheckedItems(String householdId);
    
    @Query("DELETE FROM grocery_entries WHERE household_id = :householdId")
    void deleteAllForHousehold(String householdId);
}



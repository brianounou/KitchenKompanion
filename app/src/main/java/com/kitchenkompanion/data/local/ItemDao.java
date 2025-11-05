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
 * DAO for pantry items.
 */
@Dao
public interface ItemDao {
    
    @Query("SELECT * FROM items WHERE household_id = :householdId AND is_deleted = 0 ORDER BY expiry_date ASC")
    LiveData<List<ItemEntity>> getAllItems(String householdId);
    
    @Query("SELECT * FROM items WHERE household_id = :householdId AND is_deleted = 0 ORDER BY expiry_date ASC")
    List<ItemEntity> getAllItemsSync(String householdId);
    
    @Query("SELECT * FROM items WHERE id = :itemId")
    LiveData<ItemEntity> getItemById(String itemId);
    
    @Query("SELECT * FROM items WHERE id = :itemId")
    ItemEntity getItemByIdSync(String itemId);
    
    @Query("SELECT * FROM items WHERE household_id = :householdId AND location = :location AND is_deleted = 0 ORDER BY expiry_date ASC")
    LiveData<List<ItemEntity>> getItemsByLocation(String householdId, String location);
    
    @Query("SELECT * FROM items WHERE household_id = :householdId AND expiry_date <= :date AND is_deleted = 0 ORDER BY expiry_date ASC")
    LiveData<List<ItemEntity>> getExpiringItems(String householdId, Date date);
    
    @Query("SELECT * FROM items WHERE household_id = :householdId AND expiry_date <= :date AND is_deleted = 0 ORDER BY expiry_date ASC")
    List<ItemEntity> getExpiringItemsSync(String householdId, Date date);
    
    @Query("SELECT * FROM items WHERE household_id = :householdId AND quantity <= low_stock_threshold AND is_deleted = 0")
    List<ItemEntity> getLowStockItemsSync(String householdId);
    
    @Query("SELECT * FROM items WHERE is_synced = 0")
    List<ItemEntity> getUnsyncedItems();
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ItemEntity item);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ItemEntity> items);
    
    @Update
    void update(ItemEntity item);
    
    @Delete
    void delete(ItemEntity item);
    
    @Query("UPDATE items SET is_deleted = 1, updated_at = :timestamp WHERE id = :itemId")
    void softDelete(String itemId, Date timestamp);
    
    @Query("UPDATE items SET is_synced = 1 WHERE id = :itemId")
    void markAsSynced(String itemId);
    
    @Query("DELETE FROM items WHERE household_id = :householdId")
    void deleteAllForHousehold(String householdId);
}








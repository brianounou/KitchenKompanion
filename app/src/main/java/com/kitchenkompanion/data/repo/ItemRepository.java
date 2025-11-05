package com.kitchenkompanion.data.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.kitchenkompanion.data.local.AppDatabase;
import com.kitchenkompanion.data.local.ItemDao;
import com.kitchenkompanion.data.local.ItemEntity;
import com.kitchenkompanion.utils.DateUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for pantry items.
 * Coordinates between Room (local) and Firestore (remote) via WorkManager.
 */
public class ItemRepository {
    
    private final ItemDao itemDao;
    private final WorkManager workManager;
    private final ExecutorService executorService;
    
    public ItemRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.itemDao = database.itemDao();
        this.workManager = WorkManager.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Get all items for a household
     */
    public LiveData<List<ItemEntity>> getAllItems(String householdId) {
        return itemDao.getAllItems(householdId);
    }
    
    /**
     * Get items by location
     */
    public LiveData<List<ItemEntity>> getItemsByLocation(String householdId, String location) {
        return itemDao.getItemsByLocation(householdId, location);
    }
    
    /**
     * Get items expiring within N days
     */
    public LiveData<List<ItemEntity>> getExpiringItems(String householdId, int days) {
        Date expiryDate = DateUtils.addDays(new Date(), days);
        return itemDao.getExpiringItems(householdId, expiryDate);
    }
    
    /**
     * Get a single item by ID
     */
    public LiveData<ItemEntity> getItemById(String itemId) {
        return itemDao.getItemById(itemId);
    }
    
    /**
     * Insert a new item
     */
    public void insert(ItemEntity item, String householdId) {
        executorService.execute(() -> {
            if (item.id == null || item.id.isEmpty()) {
                item.id = UUID.randomUUID().toString();
            }
            item.householdId = householdId;
            item.createdAt = new Date();
            item.updatedAt = new Date();
            item.isSynced = false;
            itemDao.insert(item);
            
            // Trigger sync
            scheduleSyncWork(householdId);
        });
    }
    
    /**
     * Update an existing item
     */
    public void update(ItemEntity item, String householdId) {
        executorService.execute(() -> {
            item.updatedAt = new Date();
            item.isSynced = false;
            itemDao.update(item);
            
            // Trigger sync
            scheduleSyncWork(householdId);
        });
    }
    
    /**
     * Delete an item (soft delete)
     */
    public void delete(String itemId, String householdId) {
        executorService.execute(() -> {
            itemDao.softDelete(itemId, new Date());
            
            // Trigger sync
            scheduleSyncWork(householdId);
        });
    }
    
    /**
     * Schedule a sync work request
     */
    private void scheduleSyncWork(String householdId) {
        Data inputData = new Data.Builder()
                .putString("householdId", householdId)
                .build();
        
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        
        OneTimeWorkRequest syncWork = new OneTimeWorkRequest.Builder(FirebaseSyncWorker.class)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build();
        
        workManager.enqueue(syncWork);
    }
    
    /**
     * Force sync now
     */
    public void forceSync(String householdId) {
        scheduleSyncWork(householdId);
    }
}








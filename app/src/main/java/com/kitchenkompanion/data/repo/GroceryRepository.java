package com.kitchenkompanion.data.repo;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kitchenkompanion.KitchenKompanionApp;
import com.kitchenkompanion.data.local.AppDatabase;
import com.kitchenkompanion.data.local.GroceryDao;
import com.kitchenkompanion.data.local.GroceryEntryEntity;
import com.kitchenkompanion.data.local.ItemDao;
import com.kitchenkompanion.data.local.ItemEntity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for grocery list entries.
 */
public class GroceryRepository {
    
    private static final String TAG = "GroceryRepository";
    private final GroceryDao groceryDao;
    private final ItemDao itemDao;
    private final WorkManager workManager;
    private final ExecutorService executorService;
    private final Context context;
    
    public GroceryRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.groceryDao = database.groceryDao();
        this.itemDao = database.itemDao();
        this.workManager = WorkManager.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
        this.context = context;
    }
    
    private String getHouseholdId() {
        return KitchenKompanionApp.getInstance().getCurrentHouseholdId();
    }
    
    /**
     * Get all grocery entries for current household
     */
    public LiveData<List<GroceryEntryEntity>> getAllEntries() {
        String householdId = getHouseholdId();
        if (householdId == null) {
            Log.w(TAG, "No household ID available");
            return null;
        }
        return groceryDao.getAllEntries(householdId);
    }
    
    /**
     * Insert a new grocery entry
     */
    public void insert(GroceryEntryEntity entry) {
        String householdId = getHouseholdId();
        if (householdId == null) return;
        
        executorService.execute(() -> {
            if (entry.id == null || entry.id.isEmpty()) {
                entry.id = UUID.randomUUID().toString();
            }
            entry.householdId = householdId;
            if (entry.listId == null || entry.listId.isEmpty()) {
                entry.listId = "default"; // Default list
            }
            entry.createdAt = new Date();
            entry.updatedAt = new Date();
            entry.isSynced = false;
            groceryDao.insert(entry);
            
            scheduleSyncWork(householdId);
        });
    }
    
    /**
     * Update checked status of an entry
     */
    public void updateCheckedStatus(long entryId, boolean checked) {
        String householdId = getHouseholdId();
        if (householdId == null) return;
        
        executorService.execute(() -> {
            groceryDao.updateCheckedStatus(String.valueOf(entryId), checked, new Date());
            scheduleSyncWork(householdId);
        });
    }
    
    /**
     * Delete an entry
     */
    public void delete(long entryId) {
        String householdId = getHouseholdId();
        if (householdId == null) return;
        
        executorService.execute(() -> {
            groceryDao.softDelete(String.valueOf(entryId), new Date());
            scheduleSyncWork(householdId);
        });
    }
    
    /**
     * Delete all checked items
     */
    public void deleteCheckedItems() {
        String householdId = getHouseholdId();
        if (householdId == null) return;
        
        executorService.execute(() -> {
            groceryDao.deleteCheckedItems(householdId);
            scheduleSyncWork(householdId);
        });
    }
    
    /**
     * Auto-generate grocery items from expiring pantry items
     */
    public void generateFromExpiringItems() {
        String householdId = getHouseholdId();
        if (householdId == null) return;
        
        executorService.execute(() -> {
            // Get items expiring in the next 7 days
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 7);
            Date sevenDaysFromNow = calendar.getTime();
            
            List<ItemEntity> expiringItems = itemDao.getExpiringItemsSync(householdId, sevenDaysFromNow);
            
            for (ItemEntity item : expiringItems) {
                // Check if already in grocery list
                GroceryEntryEntity existing = groceryDao.findByNameSync(householdId, item.name);
                if (existing == null) {
                    GroceryEntryEntity entry = new GroceryEntryEntity();
                    entry.id = UUID.randomUUID().toString();
                    entry.householdId = householdId;
                    entry.listId = "default";
                    entry.name = item.name;
                    entry.quantity = item.quantity;
                    entry.unit = item.unit;
                    entry.source = "expiring";
                    entry.checked = false;
                    entry.createdAt = new Date();
                    entry.updatedAt = new Date();
                    entry.isSynced = false;
                    
                    groceryDao.insert(entry);
                }
            }
            
            Log.i(TAG, "Generated " + expiringItems.size() + " items from expiring pantry");
            scheduleSyncWork(householdId);
        });
    }
    
    /**
     * Auto-generate grocery items from low stock pantry items
     */
    public void generateFromLowStock() {
        String householdId = getHouseholdId();
        if (householdId == null) return;
        
        executorService.execute(() -> {
            List<ItemEntity> lowStockItems = itemDao.getLowStockItemsSync(householdId);
            
            for (ItemEntity item : lowStockItems) {
                // Check if already in grocery list
                GroceryEntryEntity existing = groceryDao.findByNameSync(householdId, item.name);
                if (existing == null) {
                    GroceryEntryEntity entry = new GroceryEntryEntity();
                    entry.id = UUID.randomUUID().toString();
                    entry.householdId = householdId;
                    entry.listId = "default";
                    entry.name = item.name;
                    entry.quantity = item.quantity;
                    entry.unit = item.unit;
                    entry.source = "low-stock";
                    entry.checked = false;
                    entry.createdAt = new Date();
                    entry.updatedAt = new Date();
                    entry.isSynced = false;
                    
                    groceryDao.insert(entry);
                }
            }
            
            Log.i(TAG, "Generated " + lowStockItems.size() + " items from low stock");
            scheduleSyncWork(householdId);
        });
    }
    
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
}



package com.kitchenkompanion.features.pantry;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kitchenkompanion.auth.HouseholdSelectionActivity;
import com.kitchenkompanion.data.local.ItemEntity;
import com.kitchenkompanion.data.repo.ItemRepository;

import java.util.List;

/**
 * ViewModel for the Pantry feature.
 */
public class PantryViewModel extends AndroidViewModel {
    
    private final ItemRepository repository;
    private final LiveData<List<ItemEntity>> items;
    private final String householdId;
    
    public PantryViewModel(@NonNull Application application) {
        super(application);
        repository = new ItemRepository(application);
        householdId = HouseholdSelectionActivity.getSelectedHouseholdId(application);
        
        if (householdId != null) {
            items = repository.getAllItems(householdId);
        } else {
            items = null;
        }
    }
    
    public LiveData<List<ItemEntity>> getItems() {
        return items;
    }
    
    public LiveData<List<ItemEntity>> getItemsByLocation(String location) {
        return repository.getItemsByLocation(householdId, location);
    }
    
    public LiveData<List<ItemEntity>> getExpiringItems(int days) {
        return repository.getExpiringItems(householdId, days);
    }
    
    public LiveData<ItemEntity> getItemById(String itemId) {
        return repository.getItemById(itemId);
    }
    
    public void addItem(ItemEntity item) {
        repository.insert(item, householdId);
    }
    
    public void updateItem(ItemEntity item) {
        repository.update(item, householdId);
    }
    
    public void deleteItem(String itemId) {
        repository.delete(itemId, householdId);
    }
    
    public void forceSync() {
        repository.forceSync(householdId);
    }
}



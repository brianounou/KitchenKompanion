package com.kitchenkompanion.features.grocery;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kitchenkompanion.data.local.GroceryEntryEntity;
import com.kitchenkompanion.data.repo.GroceryRepository;

import java.util.List;

/**
 * ViewModel for grocery list management.
 */
public class GroceryViewModel extends AndroidViewModel {
    
    private final GroceryRepository repository;
    private final LiveData<List<GroceryEntryEntity>> entries;
    
    public GroceryViewModel(@NonNull Application application) {
        super(application);
        repository = new GroceryRepository(application);
        entries = repository.getAllEntries();
    }
    
    public LiveData<List<GroceryEntryEntity>> getEntries() {
        return entries;
    }
    
    public void addEntry(GroceryEntryEntity entry) {
        repository.insert(entry);
    }
    
    public void updateCheckedStatus(long id, boolean checked) {
        repository.updateCheckedStatus(id, checked);
    }
    
    public void deleteEntry(long id) {
        repository.delete(id);
    }
    
    public void deleteCheckedItems() {
        repository.deleteCheckedItems();
    }
    
    public void generateFromExpiringItems() {
        repository.generateFromExpiringItems();
    }
    
    public void generateFromLowStock() {
        repository.generateFromLowStock();
    }
}







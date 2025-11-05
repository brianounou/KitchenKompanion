package com.kitchenkompanion.data.remote;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kitchenkompanion.data.local.GroceryEntryEntity;
import com.kitchenkompanion.data.local.ItemEntity;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapper class to convert between Room entities and Firestore models.
 */
public class FirestoreMapper {
    
    private static final Gson gson = new Gson();
    
    /**
     * Converts ItemEntity to FirestoreItem
     */
    public static FirestoreItem itemEntityToFirestore(ItemEntity entity) {
        if (entity == null) return null;
        
        FirestoreItem item = new FirestoreItem();
        item.id = entity.id;
        item.barcode = entity.barcode;
        item.name = entity.name;
        item.quantity = entity.quantity;
        item.unit = entity.unit;
        item.expiryDate = entity.expiryDate;
        item.location = entity.location;
        item.photoUrl = entity.photoUrl;
        item.notes = entity.notes;
        item.addedBy = entity.addedBy;
        item.createdAt = entity.createdAt;
        item.updatedAt = entity.updatedAt;
        item.lowStockThreshold = entity.lowStockThreshold;
        
        // Parse nutrition JSON
        if (entity.nutritionJson != null && !entity.nutritionJson.isEmpty()) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            item.nutrition = gson.fromJson(entity.nutritionJson, type);
        } else {
            item.nutrition = new HashMap<>();
        }
        
        return item;
    }
    
    /**
     * Converts FirestoreItem to ItemEntity
     */
    public static ItemEntity firestoreToItemEntity(FirestoreItem item, String householdId) {
        if (item == null) return null;
        
        ItemEntity entity = new ItemEntity();
        entity.id = item.id;
        entity.householdId = householdId;
        entity.barcode = item.barcode;
        entity.name = item.name;
        entity.quantity = item.quantity;
        entity.unit = item.unit;
        entity.expiryDate = item.expiryDate;
        entity.location = item.location;
        entity.photoUrl = item.photoUrl;
        entity.notes = item.notes;
        entity.addedBy = item.addedBy;
        entity.createdAt = item.createdAt != null ? item.createdAt : new Date();
        entity.updatedAt = item.updatedAt != null ? item.updatedAt : new Date();
        entity.lowStockThreshold = item.lowStockThreshold;
        entity.isSynced = true; // Just synced from Firestore
        entity.isDeleted = false;
        
        // Convert nutrition map to JSON
        if (item.nutrition != null && !item.nutrition.isEmpty()) {
            entity.nutritionJson = gson.toJson(item.nutrition);
        }
        
        return entity;
    }
    
    /**
     * Converts GroceryEntryEntity to FirestoreGroceryEntry
     */
    public static FirestoreGroceryEntry groceryEntityToFirestore(GroceryEntryEntity entity) {
        if (entity == null) return null;
        
        FirestoreGroceryEntry entry = new FirestoreGroceryEntry();
        entry.id = entity.id;
        entry.listId = entity.listId;
        entry.itemRef = entity.itemRef;
        entry.name = entity.name;
        entry.quantity = entity.quantity;
        entry.unit = entity.unit;
        entry.source = entity.source;
        entry.isChecked = entity.isChecked;
        entry.createdAt = entity.createdAt;
        entry.updatedAt = entity.updatedAt;
        
        return entry;
    }
    
    /**
     * Converts FirestoreGroceryEntry to GroceryEntryEntity
     */
    public static GroceryEntryEntity firestoreToGroceryEntity(FirestoreGroceryEntry entry, String householdId) {
        if (entry == null) return null;
        
        GroceryEntryEntity entity = new GroceryEntryEntity();
        entity.id = entry.id;
        entity.householdId = householdId;
        entity.listId = entry.listId;
        entity.itemRef = entry.itemRef;
        entity.name = entry.name;
        entity.quantity = entry.quantity;
        entity.unit = entry.unit;
        entity.source = entry.source;
        entity.isChecked = entry.isChecked;
        entity.createdAt = entry.createdAt != null ? entry.createdAt : new Date();
        entity.updatedAt = entry.updatedAt != null ? entry.updatedAt : new Date();
        entity.isSynced = true;
        entity.isDeleted = false;
        
        return entity;
    }
}








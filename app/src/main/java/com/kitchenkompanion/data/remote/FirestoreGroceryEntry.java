package com.kitchenkompanion.data.remote;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Firestore model for grocery entries.
 */
public class FirestoreGroceryEntry {
    
    @DocumentId
    public String id;
    
    public String listId;
    public String itemRef;
    public String name;
    public double quantity;
    public String unit;
    public String source;
    public boolean isChecked;
    
    @ServerTimestamp
    public Date createdAt;
    
    @ServerTimestamp
    public Date updatedAt;
    
    public FirestoreGroceryEntry() {
        // Required empty constructor for Firestore
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (id != null) map.put("id", id);
        if (listId != null) map.put("listId", listId);
        if (itemRef != null) map.put("itemRef", itemRef);
        if (name != null) map.put("name", name);
        map.put("quantity", quantity);
        if (unit != null) map.put("unit", unit);
        if (source != null) map.put("source", source);
        map.put("isChecked", isChecked);
        if (createdAt != null) map.put("createdAt", createdAt);
        if (updatedAt != null) map.put("updatedAt", updatedAt);
        return map;
    }
}








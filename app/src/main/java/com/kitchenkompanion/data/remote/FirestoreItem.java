package com.kitchenkompanion.data.remote;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Firestore model for pantry items.
 */
public class FirestoreItem {
    
    @DocumentId
    public String id;
    
    public String barcode;
    public String name;
    public double quantity;
    public String unit;
    public Date expiryDate;
    public String location;
    public String photoUrl;
    public String notes;
    public Map<String, Object> nutrition;
    public String addedBy;
    
    @ServerTimestamp
    public Date createdAt;
    
    @ServerTimestamp
    public Date updatedAt;
    
    public double lowStockThreshold;
    
    public FirestoreItem() {
        // Required empty constructor for Firestore
        this.nutrition = new HashMap<>();
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (id != null) map.put("id", id);
        if (barcode != null) map.put("barcode", barcode);
        if (name != null) map.put("name", name);
        map.put("quantity", quantity);
        if (unit != null) map.put("unit", unit);
        if (expiryDate != null) map.put("expiryDate", expiryDate);
        if (location != null) map.put("location", location);
        if (photoUrl != null) map.put("photoUrl", photoUrl);
        if (notes != null) map.put("notes", notes);
        if (nutrition != null) map.put("nutrition", nutrition);
        if (addedBy != null) map.put("addedBy", addedBy);
        if (createdAt != null) map.put("createdAt", createdAt);
        if (updatedAt != null) map.put("updatedAt", updatedAt);
        map.put("lowStockThreshold", lowStockThreshold);
        return map;
    }
}








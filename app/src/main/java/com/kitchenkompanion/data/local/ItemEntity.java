package com.kitchenkompanion.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * Room entity representing a pantry/fridge item.
 */
@Entity(
    tableName = "items",
    indices = {
        @Index(value = "barcode"),
        @Index(value = "expiry_date"),
        @Index(value = "household_id")
    }
)
public class ItemEntity {
    
    @PrimaryKey
    @ColumnInfo(name = "id")
    public String id;
    
    @ColumnInfo(name = "household_id")
    public String householdId;
    
    @ColumnInfo(name = "barcode")
    public String barcode;
    
    @ColumnInfo(name = "name")
    public String name;
    
    @ColumnInfo(name = "quantity")
    public double quantity;
    
    @ColumnInfo(name = "unit")
    public String unit;
    
    @ColumnInfo(name = "expiry_date")
    public Date expiryDate;
    
    @ColumnInfo(name = "location")
    public String location; // fridge, freezer, pantry, other
    
    @ColumnInfo(name = "photo_url")
    public String photoUrl;
    
    @ColumnInfo(name = "notes")
    public String notes;
    
    @ColumnInfo(name = "nutrition_json")
    public String nutritionJson; // Stored as JSON string
    
    @ColumnInfo(name = "added_by")
    public String addedBy; // User ID
    
    @ColumnInfo(name = "created_at")
    public Date createdAt;
    
    @ColumnInfo(name = "updated_at")
    public Date updatedAt;
    
    @ColumnInfo(name = "low_stock_threshold")
    public double lowStockThreshold;
    
    @ColumnInfo(name = "is_synced")
    public boolean isSynced;
    
    @ColumnInfo(name = "is_deleted")
    public boolean isDeleted;
    
    public ItemEntity() {
        this.isSynced = false;
        this.isDeleted = false;
    }
}








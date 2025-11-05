package com.kitchenkompanion.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * Room entity for grocery list entries.
 */
@Entity(
    tableName = "grocery_entries",
    indices = {
        @Index(value = "list_id"),
        @Index(value = "household_id")
    }
)
public class GroceryEntryEntity {
    
    @PrimaryKey
    @ColumnInfo(name = "id")
    public String id;
    
    @ColumnInfo(name = "household_id")
    public String householdId;
    
    @ColumnInfo(name = "list_id")
    public String listId; // Grocery list this belongs to
    
    @ColumnInfo(name = "item_ref")
    public String itemRef; // Reference to pantry item if applicable
    
    @ColumnInfo(name = "name")
    public String name;
    
    @ColumnInfo(name = "quantity")
    public double quantity;
    
    @ColumnInfo(name = "unit")
    public String unit;
    
    @ColumnInfo(name = "source")
    public String source; // "ai", "manual", "low_stock", "expiry"
    
    @ColumnInfo(name = "is_checked")
    public boolean isChecked;
    
    @ColumnInfo(name = "created_at")
    public Date createdAt;
    
    @ColumnInfo(name = "updated_at")
    public Date updatedAt;
    
    @ColumnInfo(name = "is_synced")
    public boolean isSynced;
    
    @ColumnInfo(name = "is_deleted")
    public boolean isDeleted;
    
    public GroceryEntryEntity() {
        this.isSynced = false;
        this.isDeleted = false;
        this.isChecked = false;
    }
}








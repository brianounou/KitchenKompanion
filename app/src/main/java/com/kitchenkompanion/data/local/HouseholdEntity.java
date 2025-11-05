package com.kitchenkompanion.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * Room entity for household information.
 */
@Entity(tableName = "households")
public class HouseholdEntity {
    
    @PrimaryKey
    @ColumnInfo(name = "id")
    public String id;
    
    @ColumnInfo(name = "name")
    public String name;
    
    @ColumnInfo(name = "owner_id")
    public String ownerId;
    
    @ColumnInfo(name = "members_json")
    public String membersJson; // Stored as JSON array of user IDs
    
    @ColumnInfo(name = "created_at")
    public Date createdAt;
    
    @ColumnInfo(name = "updated_at")
    public Date updatedAt;
    
    @ColumnInfo(name = "is_synced")
    public boolean isSynced;
    
    public HouseholdEntity() {
        this.isSynced = false;
    }
}








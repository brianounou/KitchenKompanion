package com.kitchenkompanion.data.repo;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kitchenkompanion.data.local.AppDatabase;
import com.kitchenkompanion.data.local.GroceryDao;
import com.kitchenkompanion.data.local.GroceryEntryEntity;
import com.kitchenkompanion.data.local.ItemDao;
import com.kitchenkompanion.data.local.ItemEntity;
import com.kitchenkompanion.data.remote.FirestoreGroceryEntry;
import com.kitchenkompanion.data.remote.FirestoreItem;
import com.kitchenkompanion.data.remote.FirestoreMapper;

import java.util.List;

/**
 * WorkManager worker to sync local Room database with Firestore.
 * Handles bidirectional sync with conflict resolution.
 */
public class FirebaseSyncWorker extends Worker {
    
    private static final String TAG = "FirebaseSyncWorker";
    
    private final AppDatabase database;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    
    public FirebaseSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.database = AppDatabase.getInstance(context);
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }
    
    @NonNull
    @Override
    public Result doWork() {
        try {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                Log.w(TAG, "No authenticated user, skipping sync");
                return Result.success();
            }
            
            String householdId = getInputData().getString("householdId");
            if (householdId == null || householdId.isEmpty()) {
                Log.w(TAG, "No household ID provided, skipping sync");
                return Result.success();
            }
            
            // Sync items
            syncItems(householdId);
            
            // Sync grocery entries
            syncGroceryEntries(householdId);
            
            Log.d(TAG, "Sync completed successfully");
            return Result.success();
            
        } catch (Exception e) {
            Log.e(TAG, "Sync failed", e);
            return Result.retry();
        }
    }
    
    private void syncItems(String householdId) throws Exception {
        ItemDao itemDao = database.itemDao();
        
        // 1. Push unsynced local changes to Firestore
        List<ItemEntity> unsyncedItems = itemDao.getUnsyncedItems();
        for (ItemEntity entity : unsyncedItems) {
            if (entity.householdId.equals(householdId)) {
                if (entity.isDeleted) {
                    // Delete from Firestore
                    firestore.collection("households")
                            .document(householdId)
                            .collection("items")
                            .document(entity.id)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                itemDao.markAsSynced(entity.id);
                                Log.d(TAG, "Deleted item from Firestore: " + entity.id);
                            });
                } else {
                    // Update/create in Firestore
                    FirestoreItem firestoreItem = FirestoreMapper.itemEntityToFirestore(entity);
                    firestore.collection("households")
                            .document(householdId)
                            .collection("items")
                            .document(entity.id)
                            .set(firestoreItem)
                            .addOnSuccessListener(aVoid -> {
                                itemDao.markAsSynced(entity.id);
                                Log.d(TAG, "Synced item to Firestore: " + entity.id);
                            });
                }
            }
        }
        
        // 2. Pull remote changes from Firestore
        firestore.collection("households")
                .document(householdId)
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        FirestoreItem firestoreItem = doc.toObject(FirestoreItem.class);
                        if (firestoreItem != null) {
                            ItemEntity localItem = itemDao.getItemByIdSync(firestoreItem.id);
                            
                            // Conflict resolution: server wins if local is synced, otherwise keep local
                            if (localItem == null || localItem.isSynced) {
                                ItemEntity entity = FirestoreMapper.firestoreToItemEntity(firestoreItem, householdId);
                                itemDao.insert(entity);
                                Log.d(TAG, "Pulled item from Firestore: " + entity.id);
                            } else {
                                Log.d(TAG, "Skipping item (local changes pending): " + localItem.id);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to pull items from Firestore", e));
    }
    
    private void syncGroceryEntries(String householdId) throws Exception {
        GroceryDao groceryDao = database.groceryDao();
        
        // 1. Push unsynced local changes
        List<GroceryEntryEntity> unsyncedEntries = groceryDao.getUnsyncedEntries();
        for (GroceryEntryEntity entity : unsyncedEntries) {
            if (entity.householdId.equals(householdId)) {
                if (entity.isDeleted) {
                    firestore.collection("households")
                            .document(householdId)
                            .collection("groceryLists")
                            .document(entity.listId)
                            .collection("entries")
                            .document(entity.id)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                groceryDao.markAsSynced(entity.id);
                                Log.d(TAG, "Deleted grocery entry from Firestore: " + entity.id);
                            });
                } else {
                    FirestoreGroceryEntry firestoreEntry = FirestoreMapper.groceryEntityToFirestore(entity);
                    firestore.collection("households")
                            .document(householdId)
                            .collection("groceryLists")
                            .document(entity.listId)
                            .collection("entries")
                            .document(entity.id)
                            .set(firestoreEntry)
                            .addOnSuccessListener(aVoid -> {
                                groceryDao.markAsSynced(entity.id);
                                Log.d(TAG, "Synced grocery entry to Firestore: " + entity.id);
                            });
                }
            }
        }
        
        // 2. Pull remote changes
        firestore.collection("households")
                .document(householdId)
                .collection("groceryLists")
                .get()
                .addOnSuccessListener(listSnapshots -> {
                    for (DocumentSnapshot listDoc : listSnapshots.getDocuments()) {
                        String listId = listDoc.getId();
                        
                        firestore.collection("households")
                                .document(householdId)
                                .collection("groceryLists")
                                .document(listId)
                                .collection("entries")
                                .get()
                                .addOnSuccessListener(entrySnapshots -> {
                                    for (DocumentSnapshot doc : entrySnapshots.getDocuments()) {
                                        FirestoreGroceryEntry firestoreEntry = doc.toObject(FirestoreGroceryEntry.class);
                                        if (firestoreEntry != null) {
                                            GroceryEntryEntity localEntry = groceryDao.getEntryByIdSync(firestoreEntry.id);
                                            
                                            if (localEntry == null || localEntry.isSynced) {
                                                GroceryEntryEntity entity = FirestoreMapper.firestoreToGroceryEntity(firestoreEntry, householdId);
                                                groceryDao.insert(entity);
                                                Log.d(TAG, "Pulled grocery entry from Firestore: " + entity.id);
                                            }
                                        }
                                    }
                                });
                    }
                });
    }
}








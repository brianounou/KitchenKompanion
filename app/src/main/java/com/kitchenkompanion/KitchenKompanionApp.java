package com.kitchenkompanion;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.kitchenkompanion.features.notifications.ExpiryCheckWorker;

import java.util.concurrent.TimeUnit;

/**
 * Main Application class for Kitchen Kompanion.
 * Initializes Firebase, WorkManager, notification channels, and schedules background tasks.
 */
public class KitchenKompanionApp extends Application {
    
    public static final String CHANNEL_EXPIRY_ID = "expiry_notifications";
    public static final String CHANNEL_LOW_STOCK_ID = "low_stock_notifications";
    public static final String CHANNEL_GENERAL_ID = "general_notifications";
    
    private static KitchenKompanionApp instance;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        // Enable Firestore offline persistence
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        firestore.setFirestoreSettings(settings);
        
        // Create notification channels
        createNotificationChannels();
        
        // Schedule periodic tasks
        schedulePeriodicTasks();
    }
    
    public static KitchenKompanionApp getInstance() {
        return instance;
    }
    
    /**
     * Schedule periodic background tasks.
     */
    private void schedulePeriodicTasks() {
        // Schedule daily expiry check
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // Works offline
                .setRequiresBatteryNotLow(true)
                .build();
        
        PeriodicWorkRequest expiryCheckWork = new PeriodicWorkRequest.Builder(
                ExpiryCheckWorker.class,
                1, TimeUnit.DAYS,  // Repeat every 24 hours
                15, TimeUnit.MINUTES // Flex interval
        )
        .setConstraints(constraints)
        .build();
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "expiry_check",
                ExistingPeriodicWorkPolicy.KEEP, // Don't replace if already scheduled
                expiryCheckWork
        );
    }
    
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            
            // Expiry notifications channel
            NotificationChannel expiryChannel = new NotificationChannel(
                    CHANNEL_EXPIRY_ID,
                    "Expiry Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            expiryChannel.setDescription("Notifications for items nearing expiration");
            manager.createNotificationChannel(expiryChannel);
            
            // Low stock notifications channel
            NotificationChannel lowStockChannel = new NotificationChannel(
                    CHANNEL_LOW_STOCK_ID,
                    "Low Stock Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            lowStockChannel.setDescription("Notifications for low stock items");
            manager.createNotificationChannel(lowStockChannel);
            
            // General notifications channel
            NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_GENERAL_ID,
                    "General",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            generalChannel.setDescription("General notifications");
            manager.createNotificationChannel(generalChannel);
        }
    }
}



package com.kitchenkompanion.features.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.kitchenkompanion.KitchenKompanionApp;
import com.kitchenkompanion.MainActivity;
import com.kitchenkompanion.R;
import com.kitchenkompanion.auth.HouseholdSelectionActivity;
import com.kitchenkompanion.data.local.AppDatabase;
import com.kitchenkompanion.data.local.ItemDao;
import com.kitchenkompanion.data.local.ItemEntity;
import com.kitchenkompanion.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * WorkManager worker that checks for expiring and low stock items daily.
 * 
 * Scheduling:
 * - Runs once per day (typically at midnight or app start)
 * - Checks items expiring in the next 3 days
 * - Checks items below low stock threshold
 * - Creates grouped notifications
 * 
 * Usage:
 * PeriodicWorkRequest expiryWork = new PeriodicWorkRequest.Builder(
 *     ExpiryCheckWorker.class, 1, TimeUnit.DAYS)
 *     .setConstraints(constraints)
 *     .build();
 * WorkManager.getInstance(context).enqueueUniquePeriodicWork(
 *     "expiry_check", ExistingPeriodicWorkPolicy.KEEP, expiryWork);
 */
public class ExpiryCheckWorker extends Worker {
    
    private static final String TAG = "ExpiryCheckWorker";
    private static final int NOTIFICATION_ID_EXPIRY = 1001;
    private static final int NOTIFICATION_ID_LOW_STOCK = 1002;
    private static final int EXPIRY_WARNING_DAYS = 3;
    
    public ExpiryCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        try {
            String householdId = HouseholdSelectionActivity.getSelectedHouseholdId(getApplicationContext());
            if (householdId == null) {
                Log.w(TAG, "No household selected, skipping expiry check");
                return Result.success();
            }
            
            AppDatabase database = AppDatabase.getInstance(getApplicationContext());
            ItemDao itemDao = database.itemDao();
            
            // Check expiring items
            Date warningDate = DateUtils.addDays(new Date(), EXPIRY_WARNING_DAYS);
            List<ItemEntity> expiringItems = itemDao.getExpiringItemsSync(householdId, warningDate);
            
            // Check low stock items
            List<ItemEntity> lowStockItems = itemDao.getLowStockItemsSync(householdId);
            
            // Create notifications
            if (!expiringItems.isEmpty()) {
                notifyExpiringItems(expiringItems);
            }
            
            if (!lowStockItems.isEmpty()) {
                notifyLowStockItems(lowStockItems);
            }
            
            Log.d(TAG, "Expiry check completed: " + expiringItems.size() + 
                    " expiring, " + lowStockItems.size() + " low stock");
            
            return Result.success();
            
        } catch (Exception e) {
            Log.e(TAG, "Expiry check failed", e);
            return Result.retry();
        }
    }
    
    private void notifyExpiringItems(List<ItemEntity> items) {
        // Categorize by urgency
        List<ItemEntity> expired = new ArrayList<>();
        List<ItemEntity> expiringToday = new ArrayList<>();
        List<ItemEntity> expiringSoon = new ArrayList<>();
        
        Date today = DateUtils.getToday();
        Date tomorrow = DateUtils.addDays(today, 1);
        
        for (ItemEntity item : items) {
            if (item.expiryDate != null) {
                if (DateUtils.isExpired(item.expiryDate)) {
                    expired.add(item);
                } else if (item.expiryDate.before(tomorrow)) {
                    expiringToday.add(item);
                } else {
                    expiringSoon.add(item);
                }
            }
        }
        
        // Build notification content
        StringBuilder contentText = new StringBuilder();
        String title;
        
        if (!expired.isEmpty()) {
            title = getApplicationContext().getString(R.string.notif_expired_items);
            contentText.append(expired.size()).append(" expired");
        } else if (!expiringToday.isEmpty()) {
            title = getApplicationContext().getString(R.string.notif_expiring_today);
            contentText.append(expiringToday.size()).append(" expiring today");
        } else {
            title = getApplicationContext().getString(R.string.notif_expiry_title);
            contentText.append(expiringSoon.size()).append(" expiring soon");
        }
        
        // Add item names (up to 3)
        List<ItemEntity> topItems = !expired.isEmpty() ? expired : 
                                    !expiringToday.isEmpty() ? expiringToday : expiringSoon;
        contentText.append(": ");
        int count = 0;
        for (ItemEntity item : topItems) {
            if (count > 0) contentText.append(", ");
            contentText.append(item.name);
            if (++count >= 3) break;
        }
        if (topItems.size() > 3) {
            contentText.append(" and ").append(topItems.size() - 3).append(" more");
        }
        
        // Create intent to open pantry
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Build notification
        Notification notification = new NotificationCompat.Builder(
                getApplicationContext(), KitchenKompanionApp.CHANNEL_EXPIRY_ID)
                .setSmallIcon(R.drawable.ic_pantry)
                .setContentTitle(title)
                .setContentText(contentText.toString())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText.toString()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        
        NotificationManager manager = (NotificationManager) 
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID_EXPIRY, notification);
    }
    
    private void notifyLowStockItems(List<ItemEntity> items) {
        // Build notification content
        StringBuilder contentText = new StringBuilder();
        contentText.append(items.size()).append(" items running low: ");
        
        int count = 0;
        for (ItemEntity item : items) {
            if (count > 0) contentText.append(", ");
            contentText.append(item.name);
            if (++count >= 3) break;
        }
        if (items.size() > 3) {
            contentText.append(" and ").append(items.size() - 3).append(" more");
        }
        
        // Create intent
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(), 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Build notification
        Notification notification = new NotificationCompat.Builder(
                getApplicationContext(), KitchenKompanionApp.CHANNEL_LOW_STOCK_ID)
                .setSmallIcon(R.drawable.ic_grocery)
                .setContentTitle(getApplicationContext().getString(R.string.notif_low_stock_title))
                .setContentText(contentText.toString())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText.toString()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        
        NotificationManager manager = (NotificationManager) 
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID_LOW_STOCK, notification);
    }
}







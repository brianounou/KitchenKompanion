package com.kitchenkompanion.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for date operations.
 */
public class DateUtils {
    
    private static final SimpleDateFormat DATE_FORMAT = 
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat DISPLAY_FORMAT = 
            new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    private static final SimpleDateFormat TIMESTAMP_FORMAT = 
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    
    /**
     * Formats a date to ISO 8601 string (yyyy-MM-dd)
     */
    public static String formatDate(Date date) {
        if (date == null) return null;
        return DATE_FORMAT.format(date);
    }
    
    /**
     * Formats a date for display (MMM dd, yyyy)
     */
    public static String formatForDisplay(Date date) {
        if (date == null) return "";
        return DISPLAY_FORMAT.format(date);
    }
    
    /**
     * Formats a date to timestamp string
     */
    public static String formatTimestamp(Date date) {
        if (date == null) return null;
        return TIMESTAMP_FORMAT.format(date);
    }
    
    /**
     * Parses an ISO 8601 date string
     */
    public static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
    
    /**
     * Parses a timestamp string
     */
    public static Date parseTimestamp(String timestampStr) {
        if (timestampStr == null || timestampStr.isEmpty()) return null;
        try {
            return TIMESTAMP_FORMAT.parse(timestampStr);
        } catch (ParseException e) {
            return null;
        }
    }
    
    /**
     * Returns the number of days until the given date
     */
    public static long daysUntil(Date date) {
        if (date == null) return Long.MAX_VALUE;
        long diff = date.getTime() - System.currentTimeMillis();
        return TimeUnit.MILLISECONDS.toDays(diff);
    }
    
    /**
     * Returns the number of days between two dates
     */
    public static long daysBetween(Date start, Date end) {
        if (start == null || end == null) return 0;
        long diff = end.getTime() - start.getTime();
        return TimeUnit.MILLISECONDS.toDays(diff);
    }
    
    /**
     * Checks if a date is expired (before today)
     */
    public static boolean isExpired(Date date) {
        if (date == null) return false;
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return date.before(today.getTime());
    }
    
    /**
     * Checks if a date is expiring soon (within N days)
     */
    public static boolean isExpiringSoon(Date date, int days) {
        if (date == null) return false;
        long daysUntil = daysUntil(date);
        return daysUntil >= 0 && daysUntil <= days;
    }
    
    /**
     * Returns today's date at midnight
     */
    public static Date getToday() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today.getTime();
    }
    
    /**
     * Adds days to a date
     */
    public static Date addDays(Date date, int days) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }
}








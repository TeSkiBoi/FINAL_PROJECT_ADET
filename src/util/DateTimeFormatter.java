package util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for formatting dates and times with HTML span elements
 * to ensure correct format display in UI components.
 */
public class DateTimeFormatter {
    
    // Date format: YYYY-MM-DD
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    // Time format: HH:MM:SS
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    
    // DateTime format: YYYY-MM-DD HH:MM:SS
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // DateTime format with AM/PM: YYYY-MM-DD hh:mm:ss a
    private static final SimpleDateFormat DATETIME_12H_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
    
    /**
     * Format a Date to a string with date only (YYYY-MM-DD)
     * @param date The date to format
     * @return Formatted date string
     */
    public static String formatDate(Date date) {
        if (date == null) return "";
        return DATE_FORMAT.format(date);
    }
    
    /**
     * Format a Timestamp to a string with date only (YYYY-MM-DD)
     * @param timestamp The timestamp to format
     * @return Formatted date string
     */
    public static String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "";
        return DATE_FORMAT.format(timestamp);
    }
    
    /**
     * Format a Date to a string with time only (HH:MM:SS)
     * @param date The date to format
     * @return Formatted time string
     */
    public static String formatTime(Date date) {
        if (date == null) return "";
        return TIME_FORMAT.format(date);
    }
    
    /**
     * Format a Timestamp to a string with time only (HH:MM:SS)
     * @param timestamp The timestamp to format
     * @return Formatted time string
     */
    public static String formatTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        return TIME_FORMAT.format(timestamp);
    }
    
    /**
     * Format a Date to a string with date and time (YYYY-MM-DD HH:MM:SS)
     * @param date The date to format
     * @return Formatted datetime string
     */
    public static String formatDateTime(Date date) {
        if (date == null) return "";
        return DATETIME_FORMAT.format(date);
    }
    
    /**
     * Format a Timestamp to a string with date and time (YYYY-MM-DD HH:MM:SS)
     * @param timestamp The timestamp to format
     * @return Formatted datetime string
     */
    public static String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        return DATETIME_FORMAT.format(timestamp);
    }
    
    /**
     * Format a Date to a string with date and time in 12-hour format (YYYY-MM-DD hh:mm:ss AM/PM)
     * @param date The date to format
     * @return Formatted datetime string with AM/PM
     */
    public static String formatDateTime12H(Date date) {
        if (date == null) return "";
        return DATETIME_12H_FORMAT.format(date);
    }
    
    /**
     * Format a Timestamp to a string with date and time in 12-hour format (YYYY-MM-DD hh:mm:ss AM/PM)
     * @param timestamp The timestamp to format
     * @return Formatted datetime string with AM/PM
     */
    public static String formatDateTime12H(Timestamp timestamp) {
        if (timestamp == null) return "";
        return DATETIME_12H_FORMAT.format(timestamp);
    }
    
    /**
     * Format a Date to an HTML span with date only
     * Useful for displaying in JLabels or HTML rendering components
     * @param date The date to format
     * @return HTML span with formatted date
     */
    public static String formatDateWithSpan(Date date) {
        if (date == null) return "<span>N/A</span>";
        return "<span style='font-family: monospace;'>" + formatDate(date) + "</span>";
    }
    
    /**
     * Format a Timestamp to an HTML span with date only
     * @param timestamp The timestamp to format
     * @return HTML span with formatted date
     */
    public static String formatDateWithSpan(Timestamp timestamp) {
        if (timestamp == null) return "<span>N/A</span>";
        return "<span style='font-family: monospace;'>" + formatDate(timestamp) + "</span>";
    }
    
    /**
     * Format a Date to an HTML span with date and time
     * @param date The date to format
     * @return HTML span with formatted datetime
     */
    public static String formatDateTimeWithSpan(Date date) {
        if (date == null) return "<span>N/A</span>";
        return "<span style='font-family: monospace;'>" + formatDateTime(date) + "</span>";
    }
    
    /**
     * Format a Timestamp to an HTML span with date and time
     * @param timestamp The timestamp to format
     * @return HTML span with formatted datetime
     */
    public static String formatDateTimeWithSpan(Timestamp timestamp) {
        if (timestamp == null) return "<span>N/A</span>";
        return "<span style='font-family: monospace;'>" + formatDateTime(timestamp) + "</span>";
    }
    
    /**
     * Format a Date to an HTML span with date and time in 12-hour format
     * @param date The date to format
     * @return HTML span with formatted datetime (12-hour)
     */
    public static String formatDateTime12HWithSpan(Date date) {
        if (date == null) return "<span>N/A</span>";
        return "<span style='font-family: monospace;'>" + formatDateTime12H(date) + "</span>";
    }
    
    /**
     * Format a Timestamp to an HTML span with date and time in 12-hour format
     * @param timestamp The timestamp to format
     * @return HTML span with formatted datetime (12-hour)
     */
    public static String formatDateTime12HWithSpan(Timestamp timestamp) {
        if (timestamp == null) return "<span>N/A</span>";
        return "<span style='font-family: monospace;'>" + formatDateTime12H(timestamp) + "</span>";
    }
    
    /**
     * Get current date and time as formatted string
     * @return Current datetime formatted as YYYY-MM-DD HH:MM:SS
     */
    public static String getCurrentDateTime() {
        return formatDateTime(new Date());
    }
    
    /**
     * Get current date as formatted string
     * @return Current date formatted as YYYY-MM-DD
     */
    public static String getCurrentDate() {
        return formatDate(new Date());
    }
    
    /**
     * Get current time as formatted string
     * @return Current time formatted as HH:MM:SS
     */
    public static String getCurrentTime() {
        return formatTime(new Date());
    }
}

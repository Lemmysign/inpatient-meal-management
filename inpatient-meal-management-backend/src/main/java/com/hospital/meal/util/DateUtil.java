package com.hospital.meal.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateUtil {

    private DateUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Common date formatters
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    /**
     * Get current date
     */
    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    /**
     * Get current date-time
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    /**
     * Get current time
     */
    public static LocalTime getCurrentTime() {
        return LocalTime.now();
    }

    /**
     * Format date to string
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return null;
        return date.format(DATE_FORMATTER);
    }

    /**
     * Format date-time to string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * Format time to string
     */
    public static String formatTime(LocalTime time) {
        if (time == null) return null;
        return time.format(TIME_FORMATTER);
    }

    /**
     * Parse date from string
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) return null;
        return LocalDate.parse(dateString, DATE_FORMATTER);
    }

    /**
     * Parse date-time from string
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) return null;
        return LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
    }

    /**
     * Calculate difference in minutes between two date-times
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * Calculate difference in days between two dates
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Check if date is today
     */
    public static boolean isToday(LocalDate date) {
        if (date == null) return false;
        return date.equals(getCurrentDate());
    }

    /**
     * Check if date is in the past
     */
    public static boolean isPast(LocalDate date) {
        if (date == null) return false;
        return date.isBefore(getCurrentDate());
    }

    /**
     * Check if date is in the future
     */
    public static boolean isFuture(LocalDate date) {
        if (date == null) return false;
        return date.isAfter(getCurrentDate());
    }

    /**
     * Check if date-time is expired
     */
    public static boolean isExpired(LocalDateTime expiryDateTime) {
        if (expiryDateTime == null) return true;
        return expiryDateTime.isBefore(getCurrentDateTime());
    }

    /**
     * Add days to a date
     */
    public static LocalDate addDays(LocalDate date, long days) {
        if (date == null) return null;
        return date.plusDays(days);
    }

    /**
     * Add minutes to a date-time
     */
    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        if (dateTime == null) return null;
        return dateTime.plusMinutes(minutes);
    }

    /**
     * Get start of day
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atStartOfDay();
    }

    /**
     * Get end of day
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atTime(LocalTime.MAX);
    }

    /**
     * Check if a date is between two dates (inclusive)
     */
    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null || start == null) return false;
        if (end == null) return !date.isBefore(start);
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * Get the start of the current week (Monday)
     */
    public static LocalDate getStartOfWeek() {
        return getCurrentDate().with(DayOfWeek.MONDAY);
    }

    /**
     * Get the end of the current week (Sunday)
     */
    public static LocalDate getEndOfWeek() {
        return getCurrentDate().with(DayOfWeek.SUNDAY);
    }

    /**
     * Get the start of the current month
     */
    public static LocalDate getStartOfMonth() {
        return getCurrentDate().withDayOfMonth(1);
    }

    /**
     * Get the end of the current month
     */
    public static LocalDate getEndOfMonth() {
        return getCurrentDate().withDayOfMonth(getCurrentDate().lengthOfMonth());
    }
}
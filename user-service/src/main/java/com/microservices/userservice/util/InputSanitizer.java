package com.microservices.userservice.util;

import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

/**
 * Utility class for sanitizing user inputs to prevent injection attacks
 */
public class InputSanitizer {

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "('|(\\-\\-)|(;)|(\\|\\|)|(\\*))", Pattern.CASE_INSENSITIVE);

    /**
     * Sanitize string to prevent XSS attacks
     */
    public static String sanitizeForXSS(String input) {
        if (input == null) {
            return null;
        }
        return HtmlUtils.htmlEscape(input.trim());
    }

    /**
     * Validate that input contains only alphanumeric characters and safe symbols
     */
    public static boolean isAlphanumericSafe(String input) {
        return input != null && ALPHANUMERIC_PATTERN.matcher(input).matches();
    }

    /**
     * Check for potential SQL injection patterns (additional layer of defense)
     */
    public static boolean containsSQLInjectionPattern(String input) {
        if (input == null) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    /**
     * Sanitize username - only allow alphanumeric and underscore
     */
    public static String sanitizeUsername(String username) {
        if (username == null) {
            return null;
        }

        String trimmed = username.trim();

        // Check for SQL injection patterns
        if (containsSQLInjectionPattern(trimmed)) {
            throw new IllegalArgumentException("Invalid characters in username");
        }

        // Ensure only safe characters
        if (!isAlphanumericSafe(trimmed)) {
            throw new IllegalArgumentException("Username can only contain letters, numbers, and underscores");
        }

        return HtmlUtils.htmlEscape(trimmed);
    }

    /**
     * Sanitize email
     */
    public static String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return HtmlUtils.htmlEscape(email.trim().toLowerCase());
    }

    /**
     * Sanitize general text input
     */
    public static String sanitizeText(String text) {
        if (text == null) {
            return null;
        }

        // Remove potential SQL injection patterns
        if (containsSQLInjectionPattern(text)) {
            throw new IllegalArgumentException("Invalid characters detected");
        }

        return HtmlUtils.htmlEscape(text.trim());
    }

    /**
     * Sanitize phone number - keep only digits, optional leading '+'
     */
    public static String sanitizePhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }

        // Trim and allow only digits and '+' at start
        String trimmed = phone.trim();

        // Remove all characters except digits and plus
        String sanitized = trimmed.replaceAll("[^0-9+]", "");

        // Ensure '+' is only at the beginning if present
        if (sanitized.contains("+") && !sanitized.startsWith("+")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        // Enforce reasonable length (e.g., 7–15 digits as per E.164 standard)
        String digitsOnly = sanitized.startsWith("+") ? sanitized.substring(1) : sanitized;
        if (digitsOnly.length() < 7 || digitsOnly.length() > 15) {
            throw new IllegalArgumentException("Invalid phone number length");
        }

        return sanitized;
    }
}

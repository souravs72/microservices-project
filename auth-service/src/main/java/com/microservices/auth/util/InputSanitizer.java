package com.microservices.auth.util;

import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

public class InputSanitizer {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static String sanitizeUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }

        String trimmed = username.trim();

        if (!USERNAME_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid username format");
        }

        return HtmlUtils.htmlEscape(trimmed);
    }

    public static String sanitizeEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }

        String trimmed = email.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        return HtmlUtils.htmlEscape(trimmed);
    }

    public static String sanitizeText(String text) {
        if (text == null) {
            return null;
        }

        return HtmlUtils.htmlEscape(text.trim());
    }

    public static String sanitizePhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }

        // Remove all non-digit and non-plus characters
        String cleaned = phone.replaceAll("[^0-9+\\-()\\s]", "");
        return HtmlUtils.htmlEscape(cleaned.trim());
    }
}
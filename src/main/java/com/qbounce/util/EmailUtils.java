package com.qbounce.util;

public class EmailUtils {

    private EmailUtils() {}

    public static String extractDomain(String email) {

        if (email == null || !email.contains("@")) {
            return "";
        }

        return email.substring(email.indexOf("@") + 1)
                .toLowerCase()
                .trim();
    }
}
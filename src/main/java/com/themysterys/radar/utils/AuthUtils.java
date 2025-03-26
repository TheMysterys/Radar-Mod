package com.themysterys.radar.utils;

import java.security.SecureRandom;

public class AuthUtils {
    private static final String ACCEPTED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int SHARED_SECRET_LENGTH = 16;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();


    public static String generateSharedSecret() {
        StringBuilder sharedSecret = new StringBuilder();

        for (int i = 0; i < SHARED_SECRET_LENGTH; i++) {
            sharedSecret.append(ACCEPTED_CHARS.charAt(SECURE_RANDOM.nextInt(ACCEPTED_CHARS.length())));
        }

        return sharedSecret.toString();
    }

}

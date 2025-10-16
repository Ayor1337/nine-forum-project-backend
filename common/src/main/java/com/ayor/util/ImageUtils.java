package com.ayor.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageUtils {
    public static String extractObjectName(String url) {
        int queryIndex = url.indexOf('?');
        String cleanUrl = (queryIndex == -1) ? url : url.substring(0, queryIndex);
        String[] parts = cleanUrl.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (!parts[i].isEmpty()) {
                return parts[i];
            }
        }
        throw new IllegalArgumentException("Invalid URL format");

    }
}

package com.ayor.util;

import org.springframework.util.StringUtils;

import java.util.Locale;

public class SimpleUserAgentParser {

    private SimpleUserAgentParser() {
    }

    public static LoginDeviceInfo parse(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return new LoginDeviceInfo("Unknown", "Unknown", "Unknown");
        }
        String normalized = userAgent.toLowerCase(Locale.ROOT);
        return new LoginDeviceInfo(parseOs(normalized), parseBrowser(normalized), parseDevice(normalized));
    }

    private static String parseOs(String userAgent) {
        if (userAgent.contains("windows")) {
            return "Windows";
        }
        if (userAgent.contains("iphone") || userAgent.contains("ipad") || userAgent.contains("ios")) {
            return "iOS";
        }
        if (userAgent.contains("android")) {
            return "Android";
        }
        if (userAgent.contains("mac os") || userAgent.contains("macintosh")) {
            return "macOS";
        }
        if (userAgent.contains("linux")) {
            return "Linux";
        }
        return "Unknown";
    }

    private static String parseBrowser(String userAgent) {
        if (userAgent.contains("edg/") || userAgent.contains("edge/")) {
            return "Edge";
        }
        if (userAgent.contains("firefox/")) {
            return "Firefox";
        }
        if (userAgent.contains("chrome/") || userAgent.contains("crios/")) {
            return "Chrome";
        }
        if (userAgent.contains("safari/")) {
            return "Safari";
        }
        return "Unknown";
    }

    private static String parseDevice(String userAgent) {
        if (userAgent.contains("ipad") || userAgent.contains("tablet")) {
            return "Tablet";
        }
        if (userAgent.contains("mobile") || userAgent.contains("iphone") || userAgent.contains("android")) {
            return "Mobile";
        }
        return "Desktop";
    }
}

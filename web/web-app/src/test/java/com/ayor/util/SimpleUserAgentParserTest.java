package com.ayor.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleUserAgentParserTest {

    @Test
    void shouldParseWindowsChromeDesktop() {
        LoginDeviceInfo info = SimpleUserAgentParser.parse("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/124.0 Safari/537.36");

        assertEquals("Windows", info.osName());
        assertEquals("Chrome", info.browserName());
        assertEquals("Desktop", info.deviceType());
    }

    @Test
    void shouldParseIphoneSafariMobile() {
        LoginDeviceInfo info = SimpleUserAgentParser.parse("Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 Version/17.0 Mobile/15E148 Safari/604.1");

        assertEquals("iOS", info.osName());
        assertEquals("Safari", info.browserName());
        assertEquals("Mobile", info.deviceType());
    }
}

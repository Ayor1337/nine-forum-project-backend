package com.ayor.service;

public interface PresenceService {

    void markOnline(Integer accountId, String sessionId);

    void markOffline(Integer accountId, String sessionId);

    boolean isOnline(Integer accountId);
}

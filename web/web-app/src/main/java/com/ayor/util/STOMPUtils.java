package com.ayor.util;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class STOMPUtils {

    private final SimpUserRegistry userRegistry;

    public boolean isUserSubscribed(String username, String destinationPrefix) {
        SimpUser user = userRegistry.getUser(username);
        if (user == null) return false;

        for (SimpSession session : user.getSessions()) {
            for (SimpSubscription sub : session.getSubscriptions()) {
                String dest = sub.getDestination();
                if (dest.contains(destinationPrefix)) {
                    return true;
                }
            }
        }
        return false;
    }
}

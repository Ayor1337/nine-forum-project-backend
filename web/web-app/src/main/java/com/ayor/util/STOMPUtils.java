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

    /**
     * 判断用户是否已订阅指定前缀的目的地。
     *
     * @param userId 用户 ID
     * @param destinationPrefix 目的地前缀
     * @return 已订阅返回 true
     */
    public boolean isUserSubscribed(String userId, String destinationPrefix) {
        SimpUser user = userRegistry.getUser(userId);
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

package com.ayor.service;

import com.ayor.entity.pojo.Post;
import com.ayor.entity.pojo.Threadd;

public interface ForumRealtimeService {

    void publishThreadCreated(Threadd thread);

    void publishPostCreated(Post post);
}

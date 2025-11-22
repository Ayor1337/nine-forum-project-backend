package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Post;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PostService extends IService<Post> {

    PageEntity<Post> getPostsByThreadId(Integer threadId, Integer pageNum, Integer pageSize);

    PageEntity<Post> getPostsByAccountId(Integer accountId, Integer pageNum, Integer pageSize);

    Post getPostById(Integer postId);

    String createPost(Post post);

    String updatePost(Post post);

    String deletePost(Integer postId);
}

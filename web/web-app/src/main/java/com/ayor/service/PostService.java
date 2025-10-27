package com.ayor.service;

import com.ayor.entity.app.dto.PostDTO;
import com.ayor.entity.app.vo.PostVO;
import com.ayor.entity.pojo.Post;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PostService extends IService<Post> {
    List<PostVO> getPostsByThreadId(Integer threadId);

    String insertPost(PostDTO postDTO, String username);

    String removePostAuthorizeUsername(Integer postId, String username);

    String removePostPermission(Integer postId);
}

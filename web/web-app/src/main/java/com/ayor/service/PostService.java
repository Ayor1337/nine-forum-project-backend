package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.documennt.ThreadDoc;
import com.ayor.entity.app.dto.PostDTO;
import com.ayor.entity.app.vo.PostVO;
import com.ayor.entity.app.vo.ReplyMessageVO;
import com.ayor.entity.pojo.Post;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PostService extends IService<Post> {
    List<PostVO> getPostsByThreadId(Integer threadId);

    String insertPost(PostDTO postDTO, Integer userId);

    String removePostAuthorizeAccountId(Integer postId, Integer userId);

    String removePostPermission(Integer postId);

    PageEntity<ReplyMessageVO> listReplyMessage(Integer pageNum, Integer pageSize, Integer accountId);

    List<ThreadDoc> toThreadDoc(List<Post> posts);
}

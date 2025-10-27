package com.ayor.service.impl;

import com.ayor.entity.app.dto.PostDTO;
import com.ayor.entity.app.vo.PostVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Post;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PostMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.PostService;
import com.ayor.util.QuillUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final PostMapper postMapper;

    private final AccountMapper accountMapper;

    private final QuillUtils quillUtils;

    private final ThreaddMapper threaddMapper;


    @Override
    public List<PostVO> getPostsByThreadId(Integer threadId) {
        if (threadId == null ) {
            return null;
        }
        List<Post> posts = postMapper.getPostsByThreadId(threadId);
        List<PostVO> postVOList = new ArrayList<>();
        posts.forEach(post -> {
            PostVO postVO = new PostVO();
            if (!post.getIsDeleted()) {
                BeanUtils.copyProperties(post, postVO);
                Account account = accountMapper.getAccountById(post.getAccountId());
                postVO.setNickname(account.getNickname());
                postVO.setAccountId(account.getAccountId());
                postVO.setAvatarUrl(account.getAvatarUrl());
                postVOList.add(postVO);
            }
        });
        return postVOList;
    }

    @Override
    public String insertPost(PostDTO postDTO, String username) {
        if (postDTO.getContent() == null) {
            return "请填写内容";
        }
        if (postDTO.getThreadId() == null) {
            return "未知的发送";
        }
        Post post = new Post();
        BeanUtils.copyProperties(postDTO, post);
        Integer userID = accountMapper.getAccountIdByUsername(username);
        if (userID == null) {
            return "用户不存在";
        }
        Integer topicId = threaddMapper.getTopicIdByThreadId(postDTO.getThreadId());
        post.setAccountId(userID)   ;
        post.setContent(quillUtils.QuillDeltaConvertBase64ToURL(postDTO.getContent(), "posts/" + post.getThreadId() + "/"));
        post.setCreateTime(new Date());
        post.setTopicId(topicId);
        return this.save(post) ? null : "发布失败, 未知异常";
    }

    @Override
    public String removePostAuthorizeUsername(Integer postId, String username) {
        Post post = this.getById(postId);
        if (post == null) {
            return "帖子不存在";
        }
        if (!post.getAccountId().equals(accountMapper.getAccountIdByUsername(username))) {
            return "没有权限";
        }
        return this.removeById(postId) ? null : "删除失败, 未知异常";
    }

    @Override
    public String removePostPermission(Integer postId) {
        Post post = this.getById(postId);
        if (post == null) {
            return "帖子不存在";
        }
        return this.removeById(postId) ? null : "删除失败, 未知异常";
    }
}

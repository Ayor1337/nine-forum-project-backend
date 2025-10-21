package com.ayor.service.impl;

import com.ayor.entity.app.dto.PostDTO;
import com.ayor.entity.app.vo.PostVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Post;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PostMapper;
import com.ayor.service.PostService;
import com.ayor.util.QuillUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
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
        post.setAccountId(userID);
        post.setContent(quillUtils.QuillDeltaConvertBase64ToURL(postDTO.getContent(), "posts/" + post.getThreadId() + "/"));
        post.setCreateTime(new Date());
        return this.save(post) ? null : "发布失败, 未知异常";
    }




}

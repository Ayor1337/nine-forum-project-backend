package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Post;
import com.ayor.mapper.PostMapper;
import com.ayor.service.PostService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
@Transactional
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Override
    public PageEntity<Post> getPostsByThreadId(Integer threadId, Integer pageNum, Integer pageSize) {
        if (threadId == null) {
            return null;
        }
        Page<Post> page = this.lambdaQuery()
                .eq(Post::getThreadId, threadId)
                .eq(Post::getIsDeleted, false)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), page.getRecords());
    }

    @Override
    public PageEntity<Post> getPostsByAccountId(Integer accountId, Integer pageNum, Integer pageSize) {
        if (accountId == null) {
            return null;
        }
        Page<Post> page = this.lambdaQuery()
                .eq(Post::getAccountId, accountId)
                .eq(Post::getIsDeleted, false)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), page.getRecords());
    }

    @Override
    public Post getPostById(Integer postId) {
        if (postId == null) {
            return null;
        }
        return this.getById(postId);
    }

    @Override
    public String createPost(Post post) {
        if (post == null || post.getThreadId() == null) {
            return "帖子不存在";
        }
        if (!StringUtils.hasText(post.getContent())) {
            return "回复内容不能为空";
        }
        Date now = new Date();
        if (post.getCreateTime() == null) {
            post.setCreateTime(now);
        }
        post.setUpdateTime(now);
        post.setIsDeleted(false);
        return this.save(post) ? null : "创建回帖失败";
    }

    @Override
    public String updatePost(Post post) {
        if (post == null || post.getPostId() == null) {
            return "回复不存在";
        }
        Post exist = this.getById(post.getPostId());
        if (exist == null) {
            return "回复不存在";
        }
        if (StringUtils.hasText(post.getContent())) {
            exist.setContent(post.getContent());
        }
        exist.setUpdateTime(new Date());
        return this.updateById(exist) ? null : "更新回帖失败";
    }

    @Override
    public String deletePost(Integer postId) {
        if (postId == null) {
            return "回复不存在";
        }
        Post exist = this.getById(postId);
        if (exist == null) {
            return "回复不存在";
        }
        exist.setIsDeleted(true);
        return this.updateById(exist) ? null : "删除回复失败";
    }
}

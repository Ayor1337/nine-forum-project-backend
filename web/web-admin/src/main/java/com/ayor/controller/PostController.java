package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Post;
import com.ayor.result.Result;
import com.ayor.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/list")
    public Result<PageEntity<Post>> listPostsByThread(@RequestParam("thread_id") Integer threadId,
                                                      @RequestParam("page_num") Integer pageNum,
                                                      @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> postService.getPostsByThreadId(threadId, pageNum, pageSize), "获取帖子回复失败");
    }

    @GetMapping("/list_by_account")
    public Result<PageEntity<Post>> listPostsByAccount(@RequestParam("account_id") Integer accountId,
                                                       @RequestParam("page_num") Integer pageNum,
                                                       @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> postService.getPostsByAccountId(accountId, pageNum, pageSize), "获取用户回帖失败");
    }

    @GetMapping("/{post_id}")
    public Result<Post> getPost(@PathVariable("post_id") Integer postId) {
        return Result.dataMessageHandler(() -> postService.getPostById(postId), "获取回复失败");
    }

    @PostMapping
    public Result<Void> createPost(@RequestBody Post post) {
        return Result.messageHandler(() -> postService.createPost(post));
    }

    @PutMapping("/{post_id}")
    public Result<Void> updatePost(@PathVariable("post_id") Integer postId, @RequestBody Post post) {
        post.setPostId(postId);
        return Result.messageHandler(() -> postService.updatePost(post));
    }

    @DeleteMapping("/{post_id}")
    public Result<Void> deletePost(@PathVariable("post_id") Integer postId) {
        return Result.messageHandler(() -> postService.deletePost(postId));
    }
}

package com.ayor.controlller;

import com.ayor.entity.app.dto.PostDTO;
import com.ayor.entity.app.vo.PostVO;
import com.ayor.result.Result;
import com.ayor.service.PostService;
import com.ayor.util.SecurityUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    private final SecurityUtils security;

    @GetMapping("/info/thread")
    public Result<List<PostVO>> getPostsByThreadId(@RequestParam(name = "thread_id") String threadId) {
        return Result.dataMessageHandler(() -> postService.getPostsByThreadId(Integer.parseInt(threadId)), "获取失败");
    }

    @PostMapping("/post")
    public Result<Void> addPost(@RequestBody @Validated PostDTO post) {
        String username = security.getSecurityUsername();
        return Result.messageHandler(() -> postService.insertPost(post, username));
    }

}

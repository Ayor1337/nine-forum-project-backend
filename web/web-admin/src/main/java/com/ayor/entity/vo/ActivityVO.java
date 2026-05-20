package com.ayor.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityVO {

    private Long id;

    private LocalDateTime createdAt;

    private Long userId;

    private String username;

    private String action;

    private String target;

    private Long targetId;

    private String type;
}

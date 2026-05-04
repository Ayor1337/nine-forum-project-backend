package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountStatVO {

    private Integer userStatId;

    private Integer threadCount;

    private Integer postCount;

    private Integer replyCount;

    private Integer likedCount;

    private Integer collectedCount;

    private Integer followingCount;

    private Integer followerCount;

    private Integer accountId;
}

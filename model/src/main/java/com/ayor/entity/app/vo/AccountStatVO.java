package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountStatVO {

    private Integer accountStatId;

    private Integer threadCount;

    private Integer postCount;

    private Integer replyCount;

    private Integer likedCount;

    private Integer collectedCount;

    private Integer accountId;

}

package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountStatVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 101L;

    private Integer accountStatId;

    private Integer threadCount;

    private Integer postCount;

    private Integer replyCount;

    private Integer likedCount;

    private Integer collectedCount;

    private Integer accountId;

}

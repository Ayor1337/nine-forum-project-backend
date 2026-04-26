package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountStat {

    @TableId(type = IdType.AUTO)
    private Integer userStatId;

    private Integer threadCount;

    private Integer postCount;

    private Integer replyCount;

    private Integer likedCount;

    private Integer collectedCount;

    private Integer accountId;

}

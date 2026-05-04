package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryVO {

    private Integer historyId;

    private Integer threadId;

    private Integer accountId;

    private Date createTime;
}

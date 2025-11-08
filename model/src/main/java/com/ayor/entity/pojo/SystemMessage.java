package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemMessage {

    @TableId(type = IdType.AUTO)
    private Integer systemMessageId;

    private String title;

    private String content;

    private Date createTime;

    private Integer accountId;

    public static SystemMessage SystemMessageGroup(String title, String content) {
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setTitle(title);
        systemMessage.setContent(content);
        systemMessage.setCreateTime(new Date());
        return systemMessage;
    }

    public static SystemMessage SystemMessageIndividual(String title, String content, Integer accountId) {
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setTitle(title);
        systemMessage.setContent(content);
        systemMessage.setAccountId(accountId);
        systemMessage.setCreateTime(new Date());
        return systemMessage;
    }

}

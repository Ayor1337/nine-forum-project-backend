package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName("thread_edit_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadEditHistory {

    @TableId(type = IdType.AUTO)
    private Integer historyId;

    private Integer threadId;

    private Integer editorAccountId;

    private String title;

    private String content;

    private Date editTime;

}

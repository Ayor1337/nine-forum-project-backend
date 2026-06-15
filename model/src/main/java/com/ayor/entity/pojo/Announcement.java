package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName("announcements")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Announcement {

    @TableId(type = IdType.AUTO)
    private Integer announcementId;

    private Integer threadId;

    private Boolean isGlobal;

    private Date createTime;
}

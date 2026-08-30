package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("account_info")
public class UserProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 119L;

    @TableId
    private Integer accountId;

    private String bio;

    private String location;

    private Date birthday;

    private String website;

    private Date createTime;

    private Date updateTime;
}

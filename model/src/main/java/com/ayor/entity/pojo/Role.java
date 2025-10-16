package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("db_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @TableId(type = IdType.AUTO)
    private Integer roleId;

    private String roleName;

    private String roleNick;

    private Integer priority;

    private Integer topicId;
}

package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

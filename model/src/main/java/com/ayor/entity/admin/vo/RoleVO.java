package com.ayor.entity.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleVO {

    private Integer roleId;

    private String roleName;

    private String roleNick;

    private Integer topicId;

    private String topicName;

    private Integer priority;

}

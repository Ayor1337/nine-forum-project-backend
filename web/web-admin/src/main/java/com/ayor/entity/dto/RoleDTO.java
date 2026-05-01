package com.ayor.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDTO {

    private Integer roleId;

    private String roleName;

    private String roleNick;

    private Integer priority;

    private Integer topicId;
}

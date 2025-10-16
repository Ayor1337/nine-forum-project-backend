package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPermissionVO {

    private Integer accountId;

    private String roleName;

    private String roleNick;

    private Integer topicId;

    private List<String> permissions;
}

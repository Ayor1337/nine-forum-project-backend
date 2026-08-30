package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户权限视图对象")
public class UserPermissionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 117L;

    private Integer accountId;

    private String roleName;

    private String roleNick;

    private Integer topicId;

    private List<String> permissions;
}

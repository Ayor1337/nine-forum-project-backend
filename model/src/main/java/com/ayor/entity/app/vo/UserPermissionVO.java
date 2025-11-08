package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPermissionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 117L;

    private Integer accountId;

    private String roleName;

    private String roleNick;

    private Integer topicId;

    private List<String> permissions;
}

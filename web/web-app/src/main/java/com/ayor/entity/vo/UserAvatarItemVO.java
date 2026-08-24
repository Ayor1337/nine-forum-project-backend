package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户头像的轻量映射项。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAvatarItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 143L;

    private Integer accountId;

    private String avatarUrl;
}

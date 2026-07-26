package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAvatarVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 142L;

    private String avatarUrl;

    private String avatarFrameKey;

    private String avatarFrameName;

}

package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeThreadVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 108L;

    private Integer likeId;

    private Integer accountId;

    private Integer threadId;

}

package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeThreadVO {

    private Integer likeId;

    private Integer accountId;

    private Integer threadId;

}

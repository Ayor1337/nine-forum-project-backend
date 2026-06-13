package com.ayor.entity.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Data
@Schema(description = "用户广播请求对象")
public class UserBroadcastDTO {

    private List<Integer> accountIds;

    private String title;

    private String content;
}

package com.ayor.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserBroadcastDTO {

    private List<Integer> accountIds;

    private String title;

    private String content;
}

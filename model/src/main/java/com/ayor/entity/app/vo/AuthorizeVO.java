package com.ayor.entity.app.vo;

import lombok.Data;

import java.util.Date;

@Data
public class AuthorizeVO {
    String username;

    String role;

    String token;

    Date expire;
}

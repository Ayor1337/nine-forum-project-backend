package com.ayor.entity.app.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class AuthorizeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 103L;

    String username;

    String role;

    String token;

    Date expire;
}

package com.ayor.service;

public interface AuthorizeService {
    String createAuthorizeToken(String email);

    boolean validateAuthorizeToken(String token, String email);
}

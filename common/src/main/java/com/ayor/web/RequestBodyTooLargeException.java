package com.ayor.web;

import java.io.IOException;

/**
 * Raised as soon as a request body crosses the configured byte boundary.
 */
public class RequestBodyTooLargeException extends IOException {

    public RequestBodyTooLargeException() {
        super("请求体过大");
    }
}

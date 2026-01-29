package com.ayor.exception;

/**
 * Spotify相关异常类
 * 用于处理Spotify API调用和OAuth流程中的错误
 */
public class SpotifyException extends RuntimeException {

    public SpotifyException(String message) {
        super(message);
    }

    public SpotifyException(String message, Throwable cause) {
        super(message, cause);
    }
}

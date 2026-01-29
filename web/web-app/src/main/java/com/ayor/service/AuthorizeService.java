package com.ayor.service;

/**
 * 授权令牌服务接口
 *
 * 提供邮箱验证授权令牌的生成和验证功能。
 *
 * 主要功能:
 * - 令牌生成: 为邮箱验证生成临时授权令牌
 * - 令牌验证: 验证授权令牌的有效性
 *
 * 技术特性:
 * - 令牌存储在Redis中,具有过期时间
 * - 用于用户注册邮箱验证流程
 *
 * @author ayor
 * @since 1.0.0
 */
public interface AuthorizeService {

    /**
     * 创建邮箱验证授权令牌
     * @param email 邮箱地址
     * @return 生成的授权令牌字符串
     * @note 令牌会存储在Redis中,具有过期时间(通常为10-30分钟)
     */
    String createAuthorizeToken(String email);

    /**
     * 验证授权令牌是否有效
     * @param token 授权令牌
     * @param email 邮箱地址
     * @return true=令牌有效,false=令牌无效或已过期
     */
    boolean validateAuthorizeToken(String token, String email);
}

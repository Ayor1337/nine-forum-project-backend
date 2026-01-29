# Passkeys 实现指南

## 📋 目录
1. [依赖配置](#1-依赖配置)
2. [数据库设计](#2-数据库设计)
3. [实体类创建](#3-实体类创建)
4. [Mapper 层](#4-mapper-层)
5. [Service 层](#5-service-层)
6. [Controller 层](#6-controller-层)
7. [前端实现](#7-前端实现)
8. [Spring Security 集成](#8-spring-security-集成)
9. [测试流程](#9-测试流程)

---

## 1. 依赖配置

### Maven 依赖 (pom.xml)

在 `web/web-app/pom.xml` 中添加 Yubico WebAuthn Server 依赖：

```xml
<dependencies>
    <!-- WebAuthn Server Library -->
    <dependency>
        <groupId>com.yubico</groupId>
        <artifactId>webauthn-server-core</artifactId>
        <version>2.5.2</version>
    </dependency>

    <!-- 用于序列化 WebAuthn 数据 -->
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jdk8</artifactId>
    </dependency>

    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
    </dependency>
</dependencies>
```

---

## 2. 数据库设计

### 创建 Passkey 凭证表

```sql
CREATE TABLE `passkey_credential` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `account_id` BIGINT NOT NULL COMMENT '关联的账户ID',
    `credential_id` VARCHAR(512) NOT NULL COMMENT 'WebAuthn 凭证ID (Base64URL)',
    `public_key` TEXT NOT NULL COMMENT '公钥 (COSE 格式, Base64)',
    `signature_count` BIGINT NOT NULL DEFAULT 0 COMMENT '签名计数器',
    `aaguid` VARCHAR(64) COMMENT '认证器AAGUID',
    `user_handle` VARCHAR(512) NOT NULL COMMENT '用户句柄 (Base64URL)',
    `attestation_object` TEXT COMMENT '注册时的证明对象 (可选存储)',
    `device_name` VARCHAR(255) COMMENT '设备名称 (如: Chrome on Windows)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_account_id` (`account_id`),
    UNIQUE KEY `uk_credential_id` (`credential_id`),
    INDEX `idx_user_handle` (`user_handle`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Passkey 凭证表';
```

**字段说明**：
- `credential_id`: WebAuthn 生成的唯一凭证标识符
- `public_key`: COSE 格式的公钥，用于验证签名
- `signature_count`: 防止克隆攻击的计数器
- `user_handle`: 用户唯一标识（可以是 account_id 的 Base64 编码）
- `aaguid`: 认证器的全局唯一标识符

---

## 3. 实体类创建

### 路径：`model/src/main/java/com/ayor/entity/app/PasskeyCredential.java`

```java
package com.ayor.entity.app;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Passkey 凭证实体
 */
@Data
@TableName("passkey_credential")
public class PasskeyCredential {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的账户ID
     */
    private Long accountId;

    /**
     * WebAuthn 凭证ID (Base64URL 编码)
     */
    private String credentialId;

    /**
     * 公钥 (COSE 格式, Base64 编码)
     */
    private String publicKey;

    /**
     * 签名计数器
     */
    private Long signatureCount;

    /**
     * 认证器 AAGUID
     */
    private String aaguid;

    /**
     * 用户句柄 (Base64URL 编码)
     */
    private String userHandle;

    /**
     * 证明对象 (可选存储)
     */
    private String attestationObject;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
```

---

## 4. Mapper 层

### 路径：`web/web-app/src/main/java/com/ayor/mapper/PasskeyCredentialMapper.java`

```java
package com.ayor.mapper;

import com.ayor.entity.app.PasskeyCredential;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Passkey 凭证 Mapper
 */
@Mapper
public interface PasskeyCredentialMapper extends BaseMapper<PasskeyCredential> {
}
```

---

## 5. Service 层

### 5.1 配置类

**路径：`web/web-app/src/main/java/com/ayor/config/WebAuthnConfig.java`**

```java
package com.ayor.config;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * WebAuthn 配置类
 */
@Configuration
public class WebAuthnConfig {

    @Value("${webauthn.rp.id:localhost}")
    private String rpId;

    @Value("${webauthn.rp.name:Nine Forum}")
    private String rpName;

    @Value("${webauthn.rp.origins:http://localhost:9966}")
    private Set<String> origins;

    @Bean
    public RelyingParty relyingParty(PasskeyCredentialRepository credentialRepository) {
        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
            .id(rpId)
            .name(rpName)
            .build();

        return RelyingParty.builder()
            .identity(rpIdentity)
            .credentialRepository(credentialRepository)
            .origins(origins)
            .build();
    }
}
```

### 5.2 凭证仓库实现

**路径：`web/web-app/src/main/java/com/ayor/service/impl/PasskeyCredentialRepository.java`**

```java
package com.ayor.service.impl;

import com.ayor.entity.app.Account;
import com.ayor.entity.app.PasskeyCredential;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PasskeyCredentialMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Passkey 凭证仓库实现
 */
@Component
@RequiredArgsConstructor
public class PasskeyCredentialRepository implements CredentialRepository {

    private final PasskeyCredentialMapper credentialMapper;
    private final AccountMapper accountMapper;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        Account account = accountMapper.selectOne(
            new LambdaQueryWrapper<Account>()
                .eq(Account::getEmail, username)
        );

        if (account == null) {
            return Set.of();
        }

        PasskeyCredential credential = credentialMapper.selectOne(
            new LambdaQueryWrapper<PasskeyCredential>()
                .eq(PasskeyCredential::getAccountId, account.getId())
        );

        if (credential == null) {
            return Set.of();
        }

        return Set.of(
            PublicKeyCredentialDescriptor.builder()
                .id(ByteArray.fromBase64Url(credential.getCredentialId()))
                .build()
        );
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        Account account = accountMapper.selectOne(
            new LambdaQueryWrapper<Account>()
                .eq(Account::getEmail, username)
        );

        if (account == null) {
            return Optional.empty();
        }

        PasskeyCredential credential = credentialMapper.selectOne(
            new LambdaQueryWrapper<PasskeyCredential>()
                .eq(PasskeyCredential::getAccountId, account.getId())
        );

        return Optional.ofNullable(credential)
            .map(c -> ByteArray.fromBase64Url(c.getUserHandle()));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        PasskeyCredential credential = credentialMapper.selectOne(
            new LambdaQueryWrapper<PasskeyCredential>()
                .eq(PasskeyCredential::getUserHandle, userHandle.getBase64Url())
        );

        if (credential == null) {
            return Optional.empty();
        }

        Account account = accountMapper.selectById(credential.getAccountId());
        return Optional.ofNullable(account).map(Account::getEmail);
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        PasskeyCredential credential = credentialMapper.selectOne(
            new LambdaQueryWrapper<PasskeyCredential>()
                .eq(PasskeyCredential::getCredentialId, credentialId.getBase64Url())
                .eq(PasskeyCredential::getUserHandle, userHandle.getBase64Url())
        );

        if (credential == null) {
            return Optional.empty();
        }

        return Optional.of(
            RegisteredCredential.builder()
                .credentialId(credentialId)
                .userHandle(userHandle)
                .publicKeyCose(ByteArray.fromBase64(credential.getPublicKey()))
                .signatureCount(credential.getSignatureCount())
                .build()
        );
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        PasskeyCredential credential = credentialMapper.selectOne(
            new LambdaQueryWrapper<PasskeyCredential>()
                .eq(PasskeyCredential::getCredentialId, credentialId.getBase64Url())
        );

        if (credential == null) {
            return Set.of();
        }

        return Set.of(
            RegisteredCredential.builder()
                .credentialId(credentialId)
                .userHandle(ByteArray.fromBase64Url(credential.getUserHandle()))
                .publicKeyCose(ByteArray.fromBase64(credential.getPublicKey()))
                .signatureCount(credential.getSignatureCount())
                .build()
        );
    }
}
```

### 5.3 WebAuthn Service

**路径：`web/web-app/src/main/java/com/ayor/service/WebAuthnService.java`**

```java
package com.ayor.service;

import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.data.*;

/**
 * WebAuthn 服务接口
 */
public interface WebAuthnService {

    /**
     * 开始注册流程 - 生成注册选项
     */
    PublicKeyCredentialCreationOptions startRegistration(String username, String displayName);

    /**
     * 完成注册流程 - 验证并存储凭证
     */
    RegistrationResult finishRegistration(String username, PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential);

    /**
     * 开始认证流程 - 生成认证选项
     */
    AssertionRequest startAuthentication(String username);

    /**
     * 完成认证流程 - 验证签名
     */
    AssertionResult finishAuthentication(PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential);
}
```

### 5.4 WebAuthn Service 实现

**路径：`web/web-app/src/main/java/com/ayor/service/impl/WebAuthnServiceImpl.java`**

```java
package com.ayor.service.impl;

import com.ayor.entity.app.Account;
import com.ayor.entity.app.PasskeyCredential;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PasskeyCredentialMapper;
import com.ayor.service.WebAuthnService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebAuthn 服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebAuthnServiceImpl implements WebAuthnService {

    private final RelyingParty relyingParty;
    private final AccountMapper accountMapper;
    private final PasskeyCredentialMapper credentialMapper;

    // 临时存储注册和认证请求 (生产环境应使用 Redis)
    private final ConcurrentHashMap<String, PublicKeyCredentialCreationOptions> registrationRequests = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AssertionRequest> authenticationRequests = new ConcurrentHashMap<>();

    @Override
    public PublicKeyCredentialCreationOptions startRegistration(String username, String displayName) {
        Account account = accountMapper.selectOne(
            new LambdaQueryWrapper<Account>()
                .eq(Account::getEmail, username)
        );

        if (account == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 检查是否已有 Passkey
        PasskeyCredential existing = credentialMapper.selectOne(
            new LambdaQueryWrapper<PasskeyCredential>()
                .eq(PasskeyCredential::getAccountId, account.getId())
        );

        if (existing != null) {
            throw new IllegalStateException("该用户已注册 Passkey");
        }

        // 生成 user handle (使用 account ID)
        byte[] userHandle = String.valueOf(account.getId()).getBytes();

        UserIdentity user = UserIdentity.builder()
            .name(username)
            .displayName(displayName)
            .id(new ByteArray(userHandle))
            .build();

        StartRegistrationOptions registrationOptions = StartRegistrationOptions.builder()
            .user(user)
            .build();

        PublicKeyCredentialCreationOptions registration = relyingParty.startRegistration(registrationOptions);

        // 临时存储（生产环境应使用 Redis，设置过期时间）
        registrationRequests.put(username, registration);

        return registration;
    }

    @Override
    @Transactional
    public RegistrationResult finishRegistration(
            String username,
            PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential) {

        PublicKeyCredentialCreationOptions request = registrationRequests.remove(username);
        if (request == null) {
            throw new IllegalStateException("未找到注册请求，请重新开始注册");
        }

        try {
            FinishRegistrationOptions options = FinishRegistrationOptions.builder()
                .request(request)
                .response(credential)
                .build();

            RegistrationResult result = relyingParty.finishRegistration(options);

            // 存储凭证到数据库
            Account account = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>()
                    .eq(Account::getEmail, username)
            );

            PasskeyCredential passkey = new PasskeyCredential();
            passkey.setAccountId(account.getId());
            passkey.setCredentialId(result.getKeyId().getId().getBase64Url());
            passkey.setPublicKey(result.getPublicKeyCose().getBase64());
            passkey.setSignatureCount(result.getSignatureCount());
            passkey.setAaguid(result.getAaguid().map(ByteArray::getBase64).orElse(null));
            passkey.setUserHandle(request.getUser().getId().getBase64Url());

            credentialMapper.insert(passkey);

            log.info("用户 {} 成功注册 Passkey", username);
            return result;

        } catch (RegistrationFailedException e) {
            log.error("Passkey 注册失败: {}", e.getMessage(), e);
            throw new RuntimeException("Passkey 注册失败: " + e.getMessage(), e);
        }
    }

    @Override
    public AssertionRequest startAuthentication(String username) {
        StartAssertionOptions options = StartAssertionOptions.builder()
            .username(username)
            .build();

        AssertionRequest request = relyingParty.startAssertion(options);

        // 临时存储（生产环境应使用 Redis）
        authenticationRequests.put(username, request);

        return request;
    }

    @Override
    @Transactional
    public AssertionResult finishAuthentication(
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential) {

        // 从 credential 中获取 userHandle 来查找用户
        ByteArray userHandle = credential.getResponse().getUserHandle().orElseThrow(
            () -> new IllegalArgumentException("缺少 userHandle")
        );

        PasskeyCredential passkey = credentialMapper.selectOne(
            new LambdaQueryWrapper<PasskeyCredential>()
                .eq(PasskeyCredential::getUserHandle, userHandle.getBase64Url())
        );

        if (passkey == null) {
            throw new IllegalArgumentException("未找到对应的 Passkey");
        }

        Account account = accountMapper.selectById(passkey.getAccountId());
        String username = account.getEmail();

        AssertionRequest request = authenticationRequests.remove(username);
        if (request == null) {
            throw new IllegalStateException("未找到认证请求，请重新开始登录");
        }

        try {
            FinishAssertionOptions options = FinishAssertionOptions.builder()
                .request(request)
                .response(credential)
                .build();

            AssertionResult result = relyingParty.finishAssertion(options);

            // 更新签名计数器（防克隆）
            if (result.isSignatureCounterValid()) {
                passkey.setSignatureCount(result.getSignatureCount());
                credentialMapper.updateById(passkey);
            } else {
                log.warn("签名计数器异常，可能存在凭证克隆风险！");
            }

            log.info("用户 {} 通过 Passkey 认证成功", username);
            return result;

        } catch (AssertionFailedException e) {
            log.error("Passkey 认证失败: {}", e.getMessage(), e);
            throw new RuntimeException("Passkey 认证失败: " + e.getMessage(), e);
        }
    }
}
```

---

## 6. Controller 层

### 路径：`web/web-app/src/main/java/com/ayor/controller/PasskeyController.java`

```java
package com.ayor.controller;

import com.ayor.service.WebAuthnService;
import com.ayor.utils.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.data.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Passkey 认证控制器
 */
@RestController
@RequestMapping("/api/passkey")
@RequiredArgsConstructor
public class PasskeyController {

    private final WebAuthnService webAuthnService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /**
     * 开始注册 - 生成注册选项
     */
    @PostMapping("/register/start")
    public ResponseEntity<?> startRegistration(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String displayName = request.get("displayName");

        try {
            PublicKeyCredentialCreationOptions options = webAuthnService.startRegistration(username, displayName);

            // 序列化为 JSON
            String json = objectMapper.writeValueAsString(options);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "options", objectMapper.readValue(json, Map.class)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 完成注册 - 验证并存储凭证
     */
    @PostMapping("/register/finish")
    public ResponseEntity<?> finishRegistration(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");

        try {
            // 解析客户端响应
            String credentialJson = objectMapper.writeValueAsString(request.get("credential"));
            PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential =
                PublicKeyCredential.parseRegistrationResponseJson(credentialJson);

            RegistrationResult result = webAuthnService.finishRegistration(username, credential);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Passkey 注册成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 开始认证 - 生成认证选项
     */
    @PostMapping("/authenticate/start")
    public ResponseEntity<?> startAuthentication(@RequestBody Map<String, String> request) {
        String username = request.get("username");

        try {
            AssertionRequest assertionRequest = webAuthnService.startAuthentication(username);

            // 序列化为 JSON
            String json = objectMapper.writeValueAsString(assertionRequest.toCredentialsGetJson());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "options", objectMapper.readValue(json, Map.class)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 完成认证 - 验证签名并颁发 JWT
     */
    @PostMapping("/authenticate/finish")
    public ResponseEntity<?> finishAuthentication(@RequestBody Map<String, Object> request) {
        try {
            // 解析客户端响应
            String credentialJson = objectMapper.writeValueAsString(request.get("credential"));
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential =
                PublicKeyCredential.parseAssertionResponseJson(credentialJson);

            AssertionResult result = webAuthnService.finishAuthentication(credential);

            // 获取用户名并生成 JWT
            String username = result.getUsername();
            String token = jwtUtil.generateToken(username);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token,
                "message", "登录成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
```

---

## 7. 前端实现

### 7.1 注册流程

```javascript
/**
 * 注册 Passkey
 */
async function registerPasskey(username, displayName) {
    try {
        // 1. 获取注册选项
        const startResponse = await fetch('/api/passkey/register/start', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, displayName })
        });

        const { success, options, message } = await startResponse.json();
        if (!success) {
            throw new Error(message);
        }

        // 2. 转换 Base64URL 字段
        const publicKeyOptions = {
            ...options,
            challenge: base64UrlToUint8Array(options.challenge),
            user: {
                ...options.user,
                id: base64UrlToUint8Array(options.user.id)
            },
            excludeCredentials: options.excludeCredentials?.map(cred => ({
                ...cred,
                id: base64UrlToUint8Array(cred.id)
            })) || []
        };

        // 3. 调用浏览器 WebAuthn API
        const credential = await navigator.credentials.create({
            publicKey: publicKeyOptions
        });

        // 4. 序列化凭证响应
        const credentialJson = {
            id: credential.id,
            rawId: uint8ArrayToBase64Url(new Uint8Array(credential.rawId)),
            response: {
                attestationObject: uint8ArrayToBase64Url(new Uint8Array(credential.response.attestationObject)),
                clientDataJSON: uint8ArrayToBase64Url(new Uint8Array(credential.response.clientDataJSON))
            },
            type: credential.type
        };

        // 5. 发送到后端完成注册
        const finishResponse = await fetch('/api/passkey/register/finish', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username,
                credential: credentialJson
            })
        });

        const result = await finishResponse.json();
        if (result.success) {
            alert('Passkey 注册成功！');
        } else {
            throw new Error(result.message);
        }

    } catch (error) {
        console.error('注册失败:', error);
        alert('注册失败: ' + error.message);
    }
}
```

### 7.2 认证流程

```javascript
/**
 * 使用 Passkey 登录
 */
async function authenticateWithPasskey(username) {
    try {
        // 1. 获取认证选项
        const startResponse = await fetch('/api/passkey/authenticate/start', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username })
        });

        const { success, options, message } = await startResponse.json();
        if (!success) {
            throw new Error(message);
        }

        // 2. 转换 Base64URL 字段
        const publicKeyOptions = {
            ...options,
            challenge: base64UrlToUint8Array(options.challenge),
            allowCredentials: options.allowCredentials?.map(cred => ({
                ...cred,
                id: base64UrlToUint8Array(cred.id)
            })) || []
        };

        // 3. 调用浏览器 WebAuthn API
        const credential = await navigator.credentials.get({
            publicKey: publicKeyOptions
        });

        // 4. 序列化凭证响应
        const credentialJson = {
            id: credential.id,
            rawId: uint8ArrayToBase64Url(new Uint8Array(credential.rawId)),
            response: {
                authenticatorData: uint8ArrayToBase64Url(new Uint8Array(credential.response.authenticatorData)),
                clientDataJSON: uint8ArrayToBase64Url(new Uint8Array(credential.response.clientDataJSON)),
                signature: uint8ArrayToBase64Url(new Uint8Array(credential.response.signature)),
                userHandle: credential.response.userHandle
                    ? uint8ArrayToBase64Url(new Uint8Array(credential.response.userHandle))
                    : null
            },
            type: credential.type
        };

        // 5. 发送到后端完成认证
        const finishResponse = await fetch('/api/passkey/authenticate/finish', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ credential: credentialJson })
        });

        const result = await finishResponse.json();
        if (result.success) {
            // 存储 JWT token
            localStorage.setItem('token', result.token);
            alert('登录成功！');
            window.location.href = '/home';
        } else {
            throw new Error(result.message);
        }

    } catch (error) {
        console.error('认证失败:', error);
        alert('认证失败: ' + error.message);
    }
}
```

### 7.3 工具函数

```javascript
/**
 * Base64URL 转 Uint8Array
 */
function base64UrlToUint8Array(base64url) {
    const base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');
    const padLen = (4 - (base64.length % 4)) % 4;
    const padded = base64 + '='.repeat(padLen);
    const binary = atob(padded);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) {
        bytes[i] = binary.charCodeAt(i);
    }
    return bytes;
}

/**
 * Uint8Array 转 Base64URL
 */
function uint8ArrayToBase64Url(uint8Array) {
    let binary = '';
    for (let i = 0; i < uint8Array.byteLength; i++) {
        binary += String.fromCharCode(uint8Array[i]);
    }
    return btoa(binary)
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=/g, '');
}
```

---

## 8. Spring Security 集成

### 8.1 配置文件更新

在 `web/web-app/src/main/resources/application.yml` 中添加：

```yaml
# WebAuthn 配置
webauthn:
  rp:
    id: localhost              # 生产环境改为实际域名 (如: forum.example.com)
    name: Nine Forum
    origins: http://localhost:9966  # 生产环境改为 HTTPS
```

### 8.2 Security 配置修改

在 `SecurityConfig.java` 中放行 Passkey 相关端点：

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/passkey/**").permitAll()  // 放行 Passkey 端点
            .requestMatchers("/api/auth/**").permitAll()
            // ... 其他配置
        );

    return http.build();
}
```

---

## 9. 测试流程

### 9.1 测试准备

1. **启动应用**：
   ```bash
   cd web/web-app
   mvn spring-boot:run
   ```

2. **使用 HTTPS（重要）**：
   - Passkeys 在生产环境**必须使用 HTTPS**
   - 本地测试可用 `localhost`（浏览器允许）
   - 或使用 ngrok 创建 HTTPS 隧道

### 9.2 注册测试

1. 创建测试页面 `register.html`：
```html
<!DOCTYPE html>
<html>
<head>
    <title>注册 Passkey</title>
</head>
<body>
    <h1>注册 Passkey</h1>
    <input type="email" id="username" placeholder="邮箱" />
    <input type="text" id="displayName" placeholder="显示名称" />
    <button onclick="register()">注册</button>

    <script src="passkey.js"></script>
    <script>
        async function register() {
            const username = document.getElementById('username').value;
            const displayName = document.getElementById('displayName').value;
            await registerPasskey(username, displayName);
        }
    </script>
</body>
</html>
```

2. 测试步骤：
   - 输入已存在用户的邮箱
   - 点击注册
   - 浏览器弹出身份验证提示（指纹/Face ID/PIN）
   - 完成后查看数据库 `passkey_credential` 表

### 9.3 认证测试

1. 创建测试页面 `login.html`：
```html
<!DOCTYPE html>
<html>
<head>
    <title>Passkey 登录</title>
</head>
<body>
    <h1>Passkey 登录</h1>
    <input type="email" id="username" placeholder="邮箱" />
    <button onclick="login()">登录</button>

    <script src="passkey.js"></script>
    <script>
        async function login() {
            const username = document.getElementById('username').value;
            await authenticateWithPasskey(username);
        }
    </script>
</body>
</html>
```

2. 测试步骤：
   - 输入已注册 Passkey 的邮箱
   - 点击登录
   - 使用生物识别验证
   - 成功后获得 JWT token

---

## 10. 常见问题

### Q1: 浏览器不支持 WebAuthn 怎么办？
**A**: 检测浏览器支持：
```javascript
if (!window.PublicKeyCredential) {
    alert('您的浏览器不支持 Passkeys，请使用最新版 Chrome/Safari/Edge');
}
```

### Q2: 本地测试提示 "NotAllowedError"？
**A**: 确保：
- 使用 `https://` 或 `http://localhost`
- RP ID 配置正确（与访问域名一致）
- 用户手势触发（点击按钮调用 API）

### Q3: 如何处理一个用户多个设备？
**A**: 修改数据库约束，移除 `uk_account_id` 唯一索引，改为允许一个用户多条记录。

### Q4: 生产环境部署注意事项？
**A**:
- **必须使用 HTTPS**
- RP ID 设置为主域名（如 `forum.com`）
- 将临时存储（ConcurrentHashMap）改为 Redis
- 设置 Challenge 过期时间（建议 5 分钟）

### Q5: 如何实现"记住我"功能？
**A**: Passkeys 本身就是持久化的，无需额外"记住我"。设备会安全保存私钥。

---

## 11. 生产优化建议

### 11.1 使用 Redis 存储临时请求

```java
@Service
public class WebAuthnServiceImpl implements WebAuthnService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String REGISTRATION_PREFIX = "webauthn:reg:";
    private static final String AUTHENTICATION_PREFIX = "webauthn:auth:";
    private static final long TIMEOUT = 5; // 5 分钟

    @Override
    public PublicKeyCredentialCreationOptions startRegistration(String username, String displayName) {
        // ... 生成 options

        // 存储到 Redis
        redisTemplate.opsForValue().set(
            REGISTRATION_PREFIX + username,
            options,
            TIMEOUT,
            TimeUnit.MINUTES
        );

        return options;
    }

    @Override
    public RegistrationResult finishRegistration(String username, ...) {
        // 从 Redis 获取
        PublicKeyCredentialCreationOptions request =
            (PublicKeyCredentialCreationOptions) redisTemplate.opsForValue()
                .getAndDelete(REGISTRATION_PREFIX + username);

        // ... 验证逻辑
    }
}
```

### 11.2 添加审计日志

在 `PasskeyCredential` 表中记录：
- 最后使用时间
- 使用次数
- 设备信息（User-Agent）

### 11.3 支持降级到密码登录

```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    // 优先尝试 Passkey
    if (hasPasskey(request.getUsername())) {
        return ResponseEntity.ok(Map.of(
            "requirePasskey", true,
            "message", "请使用 Passkey 登录"
        ));
    }

    // 降级到密码登录
    return passwordLogin(request);
}
```

---

## 12. 参考资源

- [WebAuthn 规范](https://www.w3.org/TR/webauthn-2/)
- [Yubico Java WebAuthn Server](https://github.com/Yubico/java-webauthn-server)
- [MDN WebAuthn API](https://developer.mozilla.org/en-US/docs/Web/API/Web_Authentication_API)
- [Passkeys.dev 官方指南](https://passkeys.dev)

---

## 总结

完成上述步骤后，你的论坛系统将支持：
✅ Passkey 注册（与 account 表关联）
✅ Passkey 登录（替代密码）
✅ JWT token 颁发
✅ 一个用户一个 Passkey 凭证

**关键要点**：
1. 后端使用 Yubico WebAuthn Server 库处理加密验证
2. 前端调用浏览器原生 `navigator.credentials` API
3. 生产环境务必使用 HTTPS
4. 将临时存储迁移到 Redis

**下一步**：
- 添加用户管理界面（查看/删除已注册的 Passkey）
- 实现设备管理（多设备支持）
- 集成到现有的注册/登录流程

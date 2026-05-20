package com.ayor.service.impl;

import com.ayor.config.WebAuthnProperties;
import com.ayor.entity.dto.PasskeyAuthenticationFinishDTO;
import com.ayor.entity.dto.PasskeyRegistrationFinishDTO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.PasskeyCredential;
import com.ayor.entity.vo.AuthorizeVO;
import com.ayor.entity.vo.PasskeyCredentialVO;
import com.ayor.entity.vo.PasskeyOptionsVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PasskeyCredentialMapper;
import com.ayor.service.PasskeyRequestStore;
import com.ayor.service.PasskeyService;
import com.ayor.service.PasskeyWebAuthnAdapter;
import com.ayor.type.AccountStatus;
import com.ayor.util.AuthorizeResponseFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class PasskeyServiceImpl implements PasskeyService {

    private static final long TIMEOUT_MILLIS = 300_000L;

    private final PasskeyRequestStore requestStore;

    private final PasskeyCredentialMapper credentialMapper;

    private final AccountMapper accountMapper;

    private final AuthorizeResponseFactory authorizeResponseFactory;

    private final PasskeyWebAuthnAdapter webAuthnAdapter;

    private final WebAuthnProperties webAuthnProperties;

    @Autowired
    public PasskeyServiceImpl(PasskeyRequestStore requestStore,
                              PasskeyCredentialMapper credentialMapper,
                              AccountMapper accountMapper,
                              AuthorizeResponseFactory authorizeResponseFactory,
                              PasskeyWebAuthnAdapter webAuthnAdapter,
                              WebAuthnProperties webAuthnProperties) {
        this.requestStore = requestStore;
        this.credentialMapper = credentialMapper;
        this.accountMapper = accountMapper;
        this.authorizeResponseFactory = authorizeResponseFactory;
        this.webAuthnAdapter = webAuthnAdapter;
        this.webAuthnProperties = webAuthnProperties;
    }

    @Override
    public PasskeyOptionsVO<Map<String, Object>> createRegistrationOptions(Integer accountId) {
        Account account = accountMapper.getAccountById(accountId);
        if (account == null) {
            return null;
        }
        String requestId = UUID.randomUUID().toString();
        String challenge = randomBase64Url(32);
        String userHandle = Base64.getUrlEncoder().withoutPadding().encodeToString(String.valueOf(accountId).getBytes(StandardCharsets.UTF_8));
        requestStore.save(new PasskeyRequestStore.ChallengeSnapshot(
                requestId,
                PasskeyRequestStore.RequestType.REGISTRATION,
                challenge,
                webAuthnProperties.getRpId(),
                webAuthnProperties.getAllowedOrigins(),
                accountId,
                userHandle,
                Instant.now().plusSeconds(webAuthnProperties.getChallengeExpireSeconds())
        ));
        return new PasskeyOptionsVO<>(requestId, registrationPublicKey(account, challenge, userHandle));
    }

    @Override
    public String registerCredential(Integer accountId, PasskeyRegistrationFinishDTO dto) {
        PasskeyRequestStore.ChallengeSnapshot snapshot = requestStore.load(dto.getRequestId());
        if (snapshot == null || snapshot.type() != PasskeyRequestStore.RequestType.REGISTRATION || !accountId.equals(snapshot.accountId())) {
            return "Passkey 注册请求已过期";
        }
        try {
            if (credentialMapper.findByCredentialId(dto.getCredential().getRawId()) != null) {
                return "Passkey 已存在";
            }
            PasskeyCredential credential = webAuthnAdapter.verifyRegistration(accountId, dto, snapshot);
            if (credentialMapper.findByCredentialId(credential.getCredentialId()) != null) {
                return "Passkey 已存在";
            }
            credentialMapper.insert(credential);
            return null;
        } catch (RuntimeException ex) {
            log.warn("Passkey registration failed, accountId={}, requestId={}", accountId, dto.getRequestId(), ex);
            return "Passkey 注册失败";
        } finally {
            requestStore.remove(dto.getRequestId());
        }
    }

    @Override
    public List<PasskeyCredentialVO> listCredentials(Integer accountId) {
        return credentialMapper.selectList(new LambdaQueryWrapper<PasskeyCredential>()
                        .eq(PasskeyCredential::getAccountId, accountId)
                        .orderByDesc(PasskeyCredential::getCreateTime))
                .stream()
                .map(credential -> PasskeyCredentialVO.builder()
                        .credentialId(credential.getId())
                        .label(credential.getLabel())
                        .transports(splitTransports(credential.getTransports()))
                        .createTime(credential.getCreateTime())
                        .lastUsedAt(credential.getLastUsedAt())
                        .build())
                .toList();
    }

    @Override
    public String deleteCredential(Integer accountId, Long credentialId) {
        PasskeyCredential credential = credentialMapper.selectById(credentialId);
        if (credential == null) {
            return "Passkey 不存在";
        }
        if (!credential.getAccountId().equals(accountId)) {
            return "无权删除该 Passkey";
        }
        return credentialMapper.deleteById(credentialId) > 0 ? null : "删除 Passkey 失败";
    }

    @Override
    public PasskeyOptionsVO<Map<String, Object>> createAuthenticationOptions() {
        String requestId = UUID.randomUUID().toString();
        String challenge = randomBase64Url(32);
        requestStore.save(new PasskeyRequestStore.ChallengeSnapshot(
                requestId,
                PasskeyRequestStore.RequestType.AUTHENTICATION,
                challenge,
                webAuthnProperties.getRpId(),
                webAuthnProperties.getAllowedOrigins(),
                null,
                null,
                Instant.now().plusSeconds(webAuthnProperties.getChallengeExpireSeconds())
        ));
        return new PasskeyOptionsVO<>(requestId, authenticationPublicKey(challenge));
    }

    @Override
    public AuthorizeVO authenticate(PasskeyAuthenticationFinishDTO dto) {
        PasskeyRequestStore.ChallengeSnapshot snapshot = requestStore.load(dto.getRequestId());
        if (snapshot == null || snapshot.type() != PasskeyRequestStore.RequestType.AUTHENTICATION) {
            return null;
        }
        try {
            PasskeyCredential credential = credentialMapper.findByCredentialId(dto.getCredential().getRawId());
            if (credential == null || !credential.getUserHandle().equals(dto.getCredential().getResponse().getUserHandle())) {
                return null;
            }
            Long signatureCount = webAuthnAdapter.verifyAuthentication(dto, snapshot, credential);
            Account account = accountMapper.getAccountById(credential.getAccountId());
            if (account == null || AccountStatus.fromCode(account.getStatus()) == AccountStatus.BANNED) {
                return null;
            }
            credentialMapper.updateAuthenticationState(credential.getId(), signatureCount, new Date());
            return authorizeResponseFactory.create(account);
        } catch (RuntimeException ex) {
            log.warn("Passkey authentication failed, requestId={}", dto.getRequestId(), ex);
            return null;
        } finally {
            requestStore.remove(dto.getRequestId());
        }
    }

    private Map<String, Object> registrationPublicKey(Account account, String challenge, String userHandle) {
        Map<String, Object> publicKey = new LinkedHashMap<>();
        publicKey.put("rp", Map.of("id", webAuthnProperties.getRpId(), "name", webAuthnProperties.getRpName()));
        publicKey.put("user", Map.of("id", userHandle, "name", account.getUsername(), "displayName", account.getUsername()));
        publicKey.put("challenge", challenge);
        publicKey.put("pubKeyCredParams", List.of(
                Map.of("type", "public-key", "alg", -7),
                Map.of("type", "public-key", "alg", -257),
                Map.of("type", "public-key", "alg", -8)
        ));
        publicKey.put("timeout", TIMEOUT_MILLIS);
        publicKey.put("attestation", "none");
        publicKey.put("authenticatorSelection", Map.of("residentKey", "required", "userVerification", "required"));
        publicKey.put("extensions", Map.of("credProps", true));
        return publicKey;
    }

    private Map<String, Object> authenticationPublicKey(String challenge) {
        Map<String, Object> publicKey = new LinkedHashMap<>();
        publicKey.put("challenge", challenge);
        publicKey.put("rpId", webAuthnProperties.getRpId());
        publicKey.put("timeout", TIMEOUT_MILLIS);
        publicKey.put("userVerification", "required");
        return publicKey;
    }

    private static String randomBase64Url(int length) {
        byte[] bytes = new byte[length];
        new java.security.SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static List<String> splitTransports(String transports) {
        if (transports == null || transports.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(transports.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}

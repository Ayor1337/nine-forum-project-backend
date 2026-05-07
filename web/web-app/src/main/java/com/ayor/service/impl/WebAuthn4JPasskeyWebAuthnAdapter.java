package com.ayor.service.impl;

import com.ayor.entity.dto.PasskeyAuthenticationFinishDTO;
import com.ayor.entity.dto.PasskeyRegistrationFinishDTO;
import com.ayor.entity.pojo.PasskeyCredential;
import com.ayor.service.PasskeyRequestStore;
import com.ayor.service.PasskeyWebAuthnAdapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.AttestationObjectConverter;
import com.webauthn4j.converter.CollectedClientDataConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.credential.CredentialRecordImpl;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.data.PublicKeyCredentialParameters;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.RegistrationRequest;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于 WebAuthn4J 的 Passkey 校验适配器。
 *
 * <p>该实现负责把前端提交的注册断言和登录断言转换为 WebAuthn4J 可验证的数据结构，
 * 并在校验通过后返回持久化所需的 credential 信息或最新签名计数。</p>
 */
@Service
@RequiredArgsConstructor
public class WebAuthn4JPasskeyWebAuthnAdapter implements PasskeyWebAuthnAdapter {

    private final WebAuthnManager webAuthnManager;

    private final ObjectMapper objectMapper;

    private final ObjectConverter objectConverter;

    /**
     * 校验并提取注册结果。
     *
     * @param accountId 当前账号 ID
     * @param dto 注册完成请求
     * @param snapshot 对应的挑战快照
     * @return 可持久化的 passkey 凭证实体
     */
    @Override
    public PasskeyCredential verifyRegistration(Integer accountId,
                                                PasskeyRegistrationFinishDTO dto,
                                                PasskeyRequestStore.ChallengeSnapshot snapshot) {
        RegistrationRequest request = new RegistrationRequest(
                dto.getCredential().getResponse().attestationObjectBytes(),
                dto.getCredential().getResponse().clientDataJSONBytes(),
                clientExtensionsJson(dto.getCredential().getClientExtensionResults()),
                dto.getCredential().getResponse().getTransports() == null ? Set.of() : Set.copyOf(dto.getCredential().getResponse().getTransports())
        );
        RegistrationParameters parameters = new RegistrationParameters(serverProperty(snapshot), publicKeyCredentialParameters(), true, true);
        RegistrationData registrationData = webAuthnManager.verify(request, parameters);
        CredentialRecordImpl record = new CredentialRecordImpl(
                registrationData.getAttestationObject(),
                registrationData.getCollectedClientData(),
                registrationData.getClientExtensions(),
                registrationData.getTransports()
        );

        Date now = new Date();
        PasskeyCredential credential = new PasskeyCredential();
        credential.setAccountId(accountId);
        credential.setCredentialId(encode(record.getAttestedCredentialData().getCredentialId()));
        credential.setUserHandle(snapshot.userHandle());
        credential.setAttestationObject(dto.getCredential().getResponse().getAttestationObject());
        credential.setClientDataJson(dto.getCredential().getResponse().getClientDataJSON());
        credential.setSignatureCount(record.getCounter());
        credential.setTransports(registrationData.getTransports().stream().map(com.webauthn4j.data.AuthenticatorTransport::getValue).collect(Collectors.joining(",")));
        credential.setBackupEligible(record.isBackupEligible());
        credential.setBackupState(record.isBackedUp());
        credential.setUvInitialized(record.isUvInitialized());
        credential.setLabel(dto.normalizedLabel());
        credential.setCreateTime(now);
        credential.setUpdateTime(now);
        return credential;
    }

    /**
     * 校验登录断言并返回最新签名计数。
     *
     * @param dto 登录完成请求
     * @param snapshot 对应的挑战快照
     * @param credential 已持久化的 passkey 凭证
     * @return 校验后更新的签名计数
     */
    @Override
    public Long verifyAuthentication(PasskeyAuthenticationFinishDTO dto,
                                     PasskeyRequestStore.ChallengeSnapshot snapshot,
                                     PasskeyCredential credential) {
        AuthenticationRequest request = new AuthenticationRequest(
                dto.getCredential().rawIdBytes(),
                dto.getCredential().getResponse().userHandleBytes(),
                dto.getCredential().getResponse().authenticatorDataBytes(),
                dto.getCredential().getResponse().clientDataJSONBytes(),
                clientExtensionsJson(dto.getCredential().getClientExtensionResults()),
                dto.getCredential().getResponse().signatureBytes()
        );
        AuthenticationParameters parameters = new AuthenticationParameters(serverProperty(snapshot), credentialRecord(credential), null, true, true);
        return webAuthnManager.verify(request, parameters).getAuthenticatorData().getSignCount();
    }

    private CredentialRecord credentialRecord(PasskeyCredential credential) {
        AttestationObject attestationObject = new AttestationObjectConverter(objectConverter).convert(decode(credential.getAttestationObject()));
        CollectedClientData clientData = new CollectedClientDataConverter(objectConverter).convert(decode(credential.getClientDataJson()));
        Set<com.webauthn4j.data.AuthenticatorTransport> transports = credential.getTransports() == null || credential.getTransports().isBlank()
                ? Set.of()
                : java.util.Arrays.stream(credential.getTransports().split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(com.webauthn4j.data.AuthenticatorTransport::create)
                .collect(Collectors.toSet());
        return new CredentialRecordImpl(
                attestationObject.getAttestationStatement(),
                credential.getUvInitialized(),
                credential.getBackupEligible(),
                credential.getBackupState(),
                credential.getSignatureCount() == null ? 0L : credential.getSignatureCount(),
                attestationObject.getAuthenticatorData().getAttestedCredentialData(),
                attestationObject.getAuthenticatorData().getExtensions(),
                clientData,
                null,
                transports
        );
    }

    private ServerProperty serverProperty(PasskeyRequestStore.ChallengeSnapshot snapshot) {
        Set<Origin> origins = snapshot.origins().stream().map(Origin::new).collect(Collectors.toSet());
        return new ServerProperty(origins, snapshot.rpId(), new DefaultChallenge(decode(snapshot.challenge())));
    }

    private List<PublicKeyCredentialParameters> publicKeyCredentialParameters() {
        return List.of(
                new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256),
                new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.RS256),
                new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.EdDSA)
        );
    }

    private String clientExtensionsJson(com.fasterxml.jackson.databind.JsonNode clientExtensionResults) {
        if (clientExtensionResults == null || clientExtensionResults.isNull()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(clientExtensionResults);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Passkey client extension 序列化失败", ex);
        }
    }

    private static byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    private static String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}

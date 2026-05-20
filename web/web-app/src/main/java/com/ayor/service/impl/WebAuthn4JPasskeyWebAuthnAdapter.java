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
        // 前端返回的 attestationObject 和 clientDataJSON 是 WebAuthn 注册校验的核心材料；
        // transports 属于可选提示，缺失时传空集合即可，不应阻断注册校验。
        RegistrationRequest request = new RegistrationRequest(
                dto.getCredential().getResponse().attestationObjectBytes(),
                dto.getCredential().getResponse().clientDataJSONBytes(),
                clientExtensionsJson(dto.getCredential().getClientExtensionResults()),
                dto.getCredential().getResponse().getTransports() == null ? Set.of() : Set.copyOf(dto.getCredential().getResponse().getTransports())
        );

        // userVerificationRequired=true 要求认证器完成用户验证；userPresenceRequired=true 要求用户在场。
        // 这两个约束需要与 begin 注册阶段生成给浏览器的 options 保持一致。
        RegistrationParameters parameters = new RegistrationParameters(serverProperty(snapshot), publicKeyCredentialParameters(), true, true);

        // verify 会校验 challenge、origin、RP ID、attestation 数据和客户端数据；
        // 失败时 WebAuthn4J 会抛出异常，上层服务负责转换为业务错误。
        RegistrationData registrationData = webAuthnManager.verify(request, parameters);

        // CredentialRecordImpl 统一封装注册结果，便于从 WebAuthn4J 的结构中提取 credentialId、
        // 签名计数、备份状态、UV 初始化状态等需要持久化的字段。
        CredentialRecordImpl record = new CredentialRecordImpl(
                registrationData.getAttestationObject(),
                registrationData.getCollectedClientData(),
                registrationData.getClientExtensions(),
                registrationData.getTransports()
        );

        // 同一个时间戳用于创建和更新时间，避免同一次注册产生不必要的毫秒级差异。
        Date now = new Date();

        // credentialId 使用 Base64URL 无 padding 存库，与浏览器 rawId 表示保持一致；
        // attestationObject/clientDataJSON 原样保存，登录校验时会反序列化还原 CredentialRecord。
        return PasskeyCredential.builder()
                .accountId(accountId)
                .credentialId(encode(record.getAttestedCredentialData().getCredentialId()))
                .userHandle(snapshot.userHandle())
                .attestationObject(dto.getCredential().getResponse().getAttestationObject())
                .clientDataJson(dto.getCredential().getResponse().getClientDataJSON())
                .signatureCount(record.getCounter())
                // transports 在数据库中以逗号分隔存储，读取登录凭证时再转回 AuthenticatorTransport 集合。
                .transports(registrationData.getTransports().stream()
                        .map(com.webauthn4j.data.AuthenticatorTransport::getValue)
                        .collect(Collectors.joining(",")))
                .backupEligible(record.isBackupEligible())
                .backupState(record.isBackedUp())
                .uvInitialized(record.isUvInitialized())
                .label(dto.normalizedLabel())
                .createTime(now)
                .updateTime(now)
                .build();
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
        // 登录断言由认证器数据、客户端数据和签名组成；
        // rawId 用于标识当前凭证，userHandle 用于与 begin 阶段绑定的用户句柄交叉校验。
        AuthenticationRequest request = new AuthenticationRequest(
                dto.getCredential().rawIdBytes(),
                dto.getCredential().getResponse().userHandleBytes(),
                dto.getCredential().getResponse().authenticatorDataBytes(),
                dto.getCredential().getResponse().clientDataJSONBytes(),
                clientExtensionsJson(dto.getCredential().getClientExtensionResults()),
                dto.getCredential().getResponse().signatureBytes()
        );

        // credentialRecord 由数据库中的注册材料还原，WebAuthn4J 会用其中的公钥验证本次签名。
        // allowCredentials 传 null 表示不额外限制凭证列表，因为服务层已经按 rawId 查到了唯一凭证。
        AuthenticationParameters parameters = new AuthenticationParameters(serverProperty(snapshot), credentialRecord(credential), null, true, true);

        // 返回认证器最新签名计数，服务层保存后可用于后续克隆检测或风险判断。
        return webAuthnManager.verify(request, parameters).getAuthenticatorData().getSignCount();
    }

    private CredentialRecord credentialRecord(PasskeyCredential credential) {
        // WebAuthn4J 登录校验需要注册时的 attestation/client data 还原为 CredentialRecord。
        AttestationObject attestationObject = new AttestationObjectConverter(objectConverter).convert(decode(credential.getAttestationObject()));
        CollectedClientData clientData = new CollectedClientDataConverter(objectConverter).convert(decode(credential.getClientDataJson()));

        // 数据库存的是逗号分隔字符串；空值表示浏览器注册时没有提供 transports。
        Set<com.webauthn4j.data.AuthenticatorTransport> transports = credential.getTransports() == null || credential.getTransports().isBlank()
                ? Set.of()
                : java.util.Arrays.stream(credential.getTransports().split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(com.webauthn4j.data.AuthenticatorTransport::create)
                .collect(Collectors.toSet());

        // 这里不重新持久化 attestation，而是把注册时保存的关键数据组装成 WebAuthn4J 期望的凭证记录。
        // signatureCount 为空时按 0 处理，兼容旧数据或未初始化记录。
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
        // challenge、RP ID 和允许来源必须与 begin 阶段生成的快照保持一致。
        // 多 origin 支持本地开发、正式域名等场景，但必须来自服务端保存的快照，不能信任前端传入。
        Set<Origin> origins = snapshot.origins().stream().map(Origin::new).collect(Collectors.toSet());
        return new ServerProperty(origins, snapshot.rpId(), new DefaultChallenge(decode(snapshot.challenge())));
    }

    private List<PublicKeyCredentialParameters> publicKeyCredentialParameters() {
        // 只声明后端可接受的公钥算法，浏览器会在注册时据此选择支持的算法。
        return List.of(
                new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256),
                new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.RS256),
                new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.EdDSA)
        );
    }

    private String clientExtensionsJson(com.fasterxml.jackson.databind.JsonNode clientExtensionResults) {
        // WebAuthn4J 接收 JSON 字符串；前端无扩展结果时传空对象，避免 null 语义分歧。
        if (clientExtensionResults == null || clientExtensionResults.isNull()) {
            return "{}";
        }
        try {
            // 保留扩展结果原始结构交给 WebAuthn4J 解析，避免手工挑字段导致兼容性下降。
            return objectMapper.writeValueAsString(clientExtensionResults);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Passkey client extension 序列化失败", ex);
        }
    }

    private static byte[] decode(String value) {
        // WebAuthn 前端字段按 Base64URL 编码传输，不能使用普通 Base64 解码器。
        return Base64.getUrlDecoder().decode(value);
    }

    private static String encode(byte[] value) {
        // 浏览器 rawId 通常是 Base64URL 无 padding，存库也保持同一格式，便于后续按 rawId 查询。
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}

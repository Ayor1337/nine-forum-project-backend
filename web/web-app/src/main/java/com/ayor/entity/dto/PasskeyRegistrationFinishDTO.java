package com.ayor.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Base64;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * 浏览器完成 Passkey 注册后提交给后端的请求体。
 */
public class PasskeyRegistrationFinishDTO {

    @NotBlank
    private String requestId;

    @NotNull
    private Credential credential;

    private String label;

    /**
     * 返回可展示的设备标签，空值时使用默认值。
     *
     * @return 设备标签
     */
    public String normalizedLabel() {
        return this.label == null || this.label.isBlank() ? "Passkey" : this.label.trim();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    /**
     * 注册凭证对象。
     */
    public static class Credential {

        @NotBlank
        private String id;

        @NotBlank
        private String rawId;

        @NotBlank
        private String type;

        private String authenticatorAttachment;

        private JsonNode clientExtensionResults;

        @NotNull
        private AttestationResponse response;

        /**
         * 将 `rawId` 从 Base64URL 解码为字节数组。
         *
         * @return credential rawId 字节
         */
        public byte[] rawIdBytes() {
            return Base64.getUrlDecoder().decode(this.rawId);
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    /**
     * 注册阶段的 attestation 响应。
     */
    public static class AttestationResponse {

        @NotBlank
        private String clientDataJSON;

        @NotBlank
        private String attestationObject;

        private List<String> transports;

        /**
         * 将 `clientDataJSON` 从 Base64URL 解码为字节数组。
         *
         * @return clientDataJSON 字节
         */
        public byte[] clientDataJSONBytes() {
            return Base64.getUrlDecoder().decode(this.clientDataJSON);
        }

        /**
         * 将 `attestationObject` 从 Base64URL 解码为字节数组。
         *
         * @return attestationObject 字节
         */
        public byte[] attestationObjectBytes() {
            return Base64.getUrlDecoder().decode(this.attestationObject);
        }
    }
}

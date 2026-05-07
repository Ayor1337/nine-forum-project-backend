package com.ayor.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Base64;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * 浏览器完成 Passkey 登录后提交给后端的请求体。
 */
public class PasskeyAuthenticationFinishDTO {

    @NotBlank
    private String requestId;

    @NotNull
    private Credential credential;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    /**
     * 登录凭证对象。
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
        private AssertionResponse response;

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
     * 登录阶段的 assertion 响应。
     */
    public static class AssertionResponse {

        @NotBlank
        private String clientDataJSON;

        @NotBlank
        private String authenticatorData;

        @NotBlank
        private String signature;

        private String userHandle;

        /**
         * 将 `clientDataJSON` 从 Base64URL 解码为字节数组。
         *
         * @return clientDataJSON 字节
         */
        public byte[] clientDataJSONBytes() {
            return Base64.getUrlDecoder().decode(this.clientDataJSON);
        }

        /**
         * 将 `authenticatorData` 从 Base64URL 解码为字节数组。
         *
         * @return authenticatorData 字节
         */
        public byte[] authenticatorDataBytes() {
            return Base64.getUrlDecoder().decode(this.authenticatorData);
        }

        /**
         * 将 `signature` 从 Base64URL 解码为字节数组。
         *
         * @return 签名字节
         */
        public byte[] signatureBytes() {
            return Base64.getUrlDecoder().decode(this.signature);
        }

        /**
         * 将 `userHandle` 从 Base64URL 解码为字节数组。
         *
         * @return userHandle 字节，若为空则返回 `null`
         */
        public byte[] userHandleBytes() {
            return this.userHandle == null || this.userHandle.isBlank()
                    ? null
                    : Base64.getUrlDecoder().decode(this.userHandle);
        }
    }
}

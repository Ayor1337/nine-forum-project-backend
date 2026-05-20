package com.ayor.entity.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PasskeyAuthenticationFinishDTOTest {

    @Test
    void shouldDeserializeBrowserAssertionJsonAndDecodeBase64UrlFields() throws Exception {
        String json = """
                {
                  "requestId": "request-id",
                  "credential": {
                    "id": "credential-id",
                    "rawId": "AQID",
                    "type": "public-key",
                    "authenticatorAttachment": "platform",
                    "clientExtensionResults": {},
                    "response": {
                      "clientDataJSON": "BAUG",
                      "authenticatorData": "BwgJ",
                      "signature": "CgsM",
                      "userHandle": "Nw"
                    },
                    "ignored": "value"
                  }
                }
                """;

        PasskeyAuthenticationFinishDTO dto = new ObjectMapper().readValue(json, PasskeyAuthenticationFinishDTO.class);

        assertEquals("request-id", dto.getRequestId());
        assertEquals("credential-id", dto.getCredential().getId());
        assertArrayEquals(new byte[]{1, 2, 3}, dto.getCredential().rawIdBytes());
        assertArrayEquals(new byte[]{4, 5, 6}, dto.getCredential().getResponse().clientDataJSONBytes());
        assertArrayEquals(new byte[]{7, 8, 9}, dto.getCredential().getResponse().authenticatorDataBytes());
        assertArrayEquals(new byte[]{10, 11, 12}, dto.getCredential().getResponse().signatureBytes());
        assertArrayEquals(new byte[]{55}, dto.getCredential().getResponse().userHandleBytes());
    }
}

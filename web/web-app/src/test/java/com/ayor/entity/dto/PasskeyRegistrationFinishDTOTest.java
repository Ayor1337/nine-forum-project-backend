package com.ayor.entity.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PasskeyRegistrationFinishDTOTest {

    @Test
    void shouldDeserializeBrowserAttestationJsonAndIgnoreUnknownFields() throws Exception {
        String json = """
                {
                  "requestId": "request-id",
                  "label": "Chrome Passkey",
                  "credential": {
                    "id": "credential-id",
                    "rawId": "AQID",
                    "type": "public-key",
                    "authenticatorAttachment": "platform",
                    "clientExtensionResults": {
                      "credProps": {
                        "rk": true
                      }
                    },
                    "response": {
                      "clientDataJSON": "BAUG",
                      "attestationObject": "BwgJ",
                      "transports": ["internal", "hybrid"],
                      "unknownField": "ignored"
                    }
                  },
                  "unknownTopLevel": true
                }
                """;

        PasskeyRegistrationFinishDTO dto = new ObjectMapper().readValue(json, PasskeyRegistrationFinishDTO.class);

        assertEquals("request-id", dto.getRequestId());
        assertEquals("Chrome Passkey", dto.normalizedLabel());
        assertEquals("credential-id", dto.getCredential().getId());
        assertArrayEquals(new byte[]{1, 2, 3}, dto.getCredential().rawIdBytes());
        assertArrayEquals(new byte[]{4, 5, 6}, dto.getCredential().getResponse().clientDataJSONBytes());
        assertArrayEquals(new byte[]{7, 8, 9}, dto.getCredential().getResponse().attestationObjectBytes());
        assertEquals(java.util.List.of("internal", "hybrid"), dto.getCredential().getResponse().getTransports());
    }

    @Test
    void shouldUseDefaultLabelWhenMissing() {
        PasskeyRegistrationFinishDTO dto = new PasskeyRegistrationFinishDTO();

        assertEquals("Passkey", dto.normalizedLabel());
    }
}

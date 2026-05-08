package com.ayor.entity.pojo;

import com.webauthn4j.credential.CredentialRecord;
import org.apache.ibatis.reflection.Reflector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PasskeyCredentialTest {

    @Test
    void shouldRemainPlainDatabaseEntity() {
        assertFalse(CredentialRecord.class.isAssignableFrom(PasskeyCredential.class));
    }

    @Test
    void shouldExposeBooleanFieldsWithoutJavaBeanGetterConflict() {
        Reflector reflector = new Reflector(PasskeyCredential.class);
        PasskeyCredential credential = new PasskeyCredential();

        assertDoesNotThrow(() -> reflector.getGetInvoker("backupEligible").invoke(credential, null));
        assertDoesNotThrow(() -> reflector.getGetInvoker("backupState").invoke(credential, null));
        assertDoesNotThrow(() -> reflector.getGetInvoker("uvInitialized").invoke(credential, null));
    }

    @Test
    void shouldSupportBuilderCreation() {
        PasskeyCredential credential = PasskeyCredential.builder()
                .accountId(7)
                .credentialId("credential-id")
                .userHandle("user-handle")
                .signatureCount(10L)
                .backupEligible(true)
                .backupState(false)
                .uvInitialized(true)
                .build();

        assertEquals(7, credential.getAccountId());
        assertEquals("credential-id", credential.getCredentialId());
        assertEquals("user-handle", credential.getUserHandle());
        assertEquals(10L, credential.getSignatureCount());
        assertEquals(true, credential.getBackupEligible());
        assertEquals(false, credential.getBackupState());
        assertEquals(true, credential.getUvInitialized());
    }
}

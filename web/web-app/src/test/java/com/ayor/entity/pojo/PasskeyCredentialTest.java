package com.ayor.entity.pojo;

import com.webauthn4j.credential.CredentialRecord;
import org.apache.ibatis.reflection.Reflector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
}

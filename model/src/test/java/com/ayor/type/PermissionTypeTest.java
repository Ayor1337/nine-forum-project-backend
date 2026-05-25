package com.ayor.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionTypeTest {

    @Test
    void shouldRecognizeExistingPermissions() {
        assertTrue(PermissionType.isKnown("INSERT_TAG"));
        assertTrue(PermissionType.isKnown("UPDATE_TAG"));
        assertTrue(PermissionType.isKnown("DELETE_THREAD"));
    }

    @Test
    void shouldRecognizeExpandedPermissions() {
        assertTrue(PermissionType.isKnown("MANAGE_ROLE"));
        assertTrue(PermissionType.isKnown("MANAGE_PERMISSION"));
        assertTrue(PermissionType.isKnown("ASSIGN_ROLE"));
        assertTrue(PermissionType.isKnown("REVOKE_ROLE"));
        assertTrue(PermissionType.isKnown("MANAGE_ACCOUNT"));
        assertTrue(PermissionType.isKnown("CREATE_TOPIC"));
        assertTrue(PermissionType.isKnown("SET_ANNOUNCEMENT"));
        assertTrue(PermissionType.isKnown("HANDLE_REPORT"));
        assertTrue(PermissionType.isKnown("VIEW_DASHBOARD"));
    }

    @Test
    void shouldRejectUnknownPermission() {
        assertFalse(PermissionType.isKnown("UNKNOWN"));
    }
}

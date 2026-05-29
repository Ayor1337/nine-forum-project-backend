package com.ayor.service.impl;

import com.ayor.entity.dto.PasskeyAuthenticationFinishDTO;
import com.ayor.config.WebAuthnProperties;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.PasskeyCredential;
import com.ayor.entity.vo.AuthorizeVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PasskeyCredentialMapper;
import com.ayor.service.PasskeyRequestStore;
import com.ayor.service.PasskeyWebAuthnAdapter;
import com.ayor.util.AuthorizeResponseFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasskeyServiceImplTest {

    @Mock
    private PasskeyRequestStore requestStore;

    @Mock
    private PasskeyCredentialMapper credentialMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AuthorizeResponseFactory authorizeResponseFactory;

    @Mock
    private PasskeyWebAuthnAdapter webAuthnAdapter;

    @Test
    void shouldUseUsernameInRegistrationOptionsUserName() {
        PasskeyServiceImpl service = service();
        Account account = new Account();
        account.setAccountId(7);
        account.setUsername("tester");
        when(accountMapper.getAccountById(7)).thenReturn(account);

        Map<String, Object> publicKey = service.createRegistrationOptions(7).getPublicKey();
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) publicKey.get("user");

        assertEquals("tester", user.get("name"));
        assertEquals("Nw", user.get("id"));
    }

    @Test
    void shouldRejectExpiredAuthenticationRequest() {
        PasskeyServiceImpl service = service();
        PasskeyAuthenticationFinishDTO dto = authenticationDto("AQID", "Nw");

        assertNull(service.authenticate(dto, null));
    }

    @Test
    void shouldRejectAuthenticationWhenUserHandleDoesNotMatchCredentialOwner() {
        PasskeyServiceImpl service = service();
        PasskeyAuthenticationFinishDTO dto = authenticationDto("AQID", "OA");
        PasskeyCredential credential = credential(7);
        PasskeyRequestStore.ChallengeSnapshot snapshot = authenticationSnapshot();

        when(requestStore.load("req-1")).thenReturn(snapshot);
        when(credentialMapper.findByCredentialId("AQID")).thenReturn(credential);

        assertNull(service.authenticate(dto, null));
    }

    @Test
    void shouldRejectBannedAccountAuthentication() {
        PasskeyServiceImpl service = service();
        PasskeyAuthenticationFinishDTO dto = authenticationDto("AQID", "Nw");
        PasskeyCredential credential = credential(7);
        PasskeyRequestStore.ChallengeSnapshot snapshot = authenticationSnapshot();
        Account account = new Account();
        account.setAccountId(7);
        account.setStatus(3);

        when(requestStore.load("req-1")).thenReturn(snapshot);
        when(credentialMapper.findByCredentialId("AQID")).thenReturn(credential);
        when(webAuthnAdapter.verifyAuthentication(dto, snapshot, credential)).thenReturn(10L);
        when(accountMapper.getAccountById(7)).thenReturn(account);

        assertNull(service.authenticate(dto, null));
    }

    @Test
    void shouldReturnAuthorizeVoWhenAuthenticationSucceeds() {
        PasskeyServiceImpl service = service();
        PasskeyAuthenticationFinishDTO dto = authenticationDto("AQID", "Nw");
        PasskeyCredential credential = credential(7);
        PasskeyRequestStore.ChallengeSnapshot snapshot = authenticationSnapshot();
        Account account = new Account();
        account.setAccountId(7);
        account.setUsername("tester");
        AuthorizeVO authorizeVO = new AuthorizeVO();
        authorizeVO.setUsername("tester");
        authorizeVO.setToken("jwt-token");

        when(requestStore.load("req-1")).thenReturn(snapshot);
        when(credentialMapper.findByCredentialId("AQID")).thenReturn(credential);
        when(webAuthnAdapter.verifyAuthentication(dto, snapshot, credential)).thenReturn(10L);
        when(accountMapper.getAccountById(7)).thenReturn(account);
        when(authorizeResponseFactory.create(account)).thenReturn(authorizeVO);

        AuthorizeVO result = service.authenticate(dto, null);

        assertEquals("tester", result.getUsername());
        assertEquals("jwt-token", result.getToken());
        verify(credentialMapper).updateAuthenticationState(eq(credential.getId()), eq(10L), any(Date.class));
        verify(requestStore).remove("req-1");
    }

    @Test
    void shouldRejectDeletingCredentialOwnedByAnotherAccount() {
        PasskeyServiceImpl service = service();
        PasskeyCredential credential = credential(9);
        credential.setId(11L);
        when(credentialMapper.selectById(11L)).thenReturn(credential);

        String result = service.deleteCredential(7, 11L);

        assertEquals("无权删除该 Passkey", result);
    }

    private PasskeyServiceImpl service() {
        return new PasskeyServiceImpl(requestStore, credentialMapper, accountMapper, authorizeResponseFactory, webAuthnAdapter, new WebAuthnProperties());
    }

    private static PasskeyRequestStore.ChallengeSnapshot authenticationSnapshot() {
        return new PasskeyRequestStore.ChallengeSnapshot(
                "req-1",
                PasskeyRequestStore.RequestType.AUTHENTICATION,
                "challenge",
                "localhost",
                List.of("http://localhost:3000"),
                null,
                null,
                Instant.now().plusSeconds(300)
        );
    }

    private static PasskeyAuthenticationFinishDTO authenticationDto(String rawId, String userHandle) {
        PasskeyAuthenticationFinishDTO dto = new PasskeyAuthenticationFinishDTO();
        dto.setRequestId("req-1");
        PasskeyAuthenticationFinishDTO.Credential credential = new PasskeyAuthenticationFinishDTO.Credential();
        credential.setId("credential-id");
        credential.setRawId(rawId);
        credential.setType("public-key");
        PasskeyAuthenticationFinishDTO.AssertionResponse response = new PasskeyAuthenticationFinishDTO.AssertionResponse();
        response.setClientDataJSON("BAUG");
        response.setAuthenticatorData("BwgJ");
        response.setSignature("CgsM");
        response.setUserHandle(userHandle);
        credential.setResponse(response);
        dto.setCredential(credential);
        return dto;
    }

    private static PasskeyCredential credential(Integer accountId) {
        PasskeyCredential credential = new PasskeyCredential();
        credential.setId(1L);
        credential.setAccountId(accountId);
        credential.setCredentialId("AQID");
        credential.setUserHandle("Nw");
        return credential;
    }
}

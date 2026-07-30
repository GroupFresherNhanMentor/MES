package fpt.qn.mes.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.auth.application.dto.request.LoginRequest;
import fpt.qn.mes.auth.application.dto.response.TokenResponse;
import fpt.qn.mes.auth.application.exception.UnauthorizedException;
import fpt.qn.mes.auth.application.port.in.ResolveAuthorizationUseCase;
import fpt.qn.mes.auth.application.port.out.CredentialQueryPort;
import fpt.qn.mes.auth.application.port.out.PasswordPort;
import fpt.qn.mes.auth.application.port.out.TokenPort;
import fpt.qn.mes.auth.application.security.AuthorizationSnapshot;
import fpt.qn.mes.auth.application.security.CredentialAccount;
import fpt.qn.mes.auth.application.security.TokenClaims;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock TokenPort tokenPort;
    @Mock PasswordPort passwordPort;
    @Mock CredentialQueryPort credentialQueryPort;
    @Mock ResolveAuthorizationUseCase resolveAuthorizationUseCase;

    AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(
                tokenPort,
                passwordPort,
                credentialQueryPort,
                resolveAuthorizationUseCase);
    }

    @Test
    void successfulLoginReturnsIdentityAndTokensOnly() {
        UUID userId = UUID.randomUUID();
        CredentialAccount account = account(userId, true);
        AuthorizationSnapshot snapshot = snapshot(userId);
        when(credentialQueryPort.findByUsername("alice")).thenReturn(Optional.of(account));
        when(passwordPort.matches("Password@123", account.getPasswordHash())).thenReturn(true);
        when(resolveAuthorizationUseCase.resolve(userId)).thenReturn(Optional.of(snapshot));
        when(tokenPort.generateAccessToken(userId, "alice")).thenReturn("access.jwt");
        when(tokenPort.generateRefreshToken(userId, "alice")).thenReturn("refresh.jwt");

        TokenResponse response = service.login(new LoginRequest("alice", "Password@123"));

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getAccessToken()).isEqualTo("access.jwt");
        assertThat(response.getRefreshToken()).isEqualTo("refresh.jwt");
    }

    @Test
    void missingAndInactiveUsersReturnSameGenericCredentialError() {
        when(credentialQueryPort.findByUsername("missing")).thenReturn(Optional.empty());
        when(passwordPort.matches("wrong", AuthService.DUMMY_BCRYPT_HASH)).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("missing", "wrong")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid username or password");

        CredentialAccount inactive = account(UUID.randomUUID(), false);
        when(credentialQueryPort.findByUsername("alice")).thenReturn(Optional.of(inactive));
        when(passwordPort.matches("Password@123", inactive.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> service.login(new LoginRequest("alice", "Password@123")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid username or password");
        verify(tokenPort, never()).generateAccessToken(
                org.mockito.ArgumentMatchers.any(), anyString());
    }

    @Test
    void refreshValidatesJwtReloadsUserAndIssuesNewPairWithoutPersistence() {
        UUID userId = UUID.randomUUID();
        TokenClaims claims = TokenClaims.builder()
                .userId(userId)
                .username("old-username")
                .tokenId(UUID.randomUUID())
                .tokenType("refresh")
                .build();
        AuthorizationSnapshot snapshot = snapshot(userId);
        when(tokenPort.parseRefreshToken("refresh.jwt")).thenReturn(claims);
        when(resolveAuthorizationUseCase.resolve(userId)).thenReturn(Optional.of(snapshot));
        when(tokenPort.generateAccessToken(userId, "alice")).thenReturn("next-access");
        when(tokenPort.generateRefreshToken(userId, "alice")).thenReturn("next-refresh");

        TokenResponse response = service.refresh("refresh.jwt");

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getAccessToken()).isEqualTo("next-access");
        assertThat(response.getRefreshToken()).isEqualTo("next-refresh");
    }

    @Test
    void refreshRejectsUserThatNoLongerExistsOrIsInactive() {
        UUID userId = UUID.randomUUID();
        TokenClaims claims = TokenClaims.builder()
                .userId(userId)
                .username("alice")
                .tokenId(UUID.randomUUID())
                .tokenType("refresh")
                .build();
        when(tokenPort.parseRefreshToken("refresh.jwt")).thenReturn(claims);
        when(resolveAuthorizationUseCase.resolve(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh("refresh.jwt"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid refresh token");
        verify(tokenPort, never()).generateRefreshToken(
                org.mockito.ArgumentMatchers.any(), anyString());
    }

    private CredentialAccount account(UUID userId, boolean active) {
        return CredentialAccount.builder()
                .id(userId)
                .username("alice")
                .passwordHash("$2a$encoded")
                .fullName("Alice")
                .active(active)
                .build();
    }

    private AuthorizationSnapshot snapshot(UUID userId) {
        return AuthorizationSnapshot.builder()
                .userId(userId)
                .username("alice")
                .fullName("Alice")
                .active(true)
                .roles(List.of("ADMIN", "AUDITOR"))
                .build();
    }
}

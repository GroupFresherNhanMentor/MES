package fpt.qn.mes.auth.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.auth.application.port.out.RbacMutationGuardPort;
import fpt.qn.mes.common.exception.ConflictException;

@ExtendWith(MockitoExtension.class)
class AdministrativeAccessGuardTest {

    @Mock
    RbacMutationGuardPort guardPort;

    AdministrativeAccessGuard guard;

    @BeforeEach
    void setUp() {
        guard = new AdministrativeAccessGuard(guardPort);
    }

    @Test
    void delegatesSingletonLock() {
        guard.lock();
        verify(guardPort).lock();
    }

    @Test
    void acceptsWhenAnActiveEffectiveAdministratorRemains() {
        when(guardPort.hasActiveAdministrator()).thenReturn(true);
        guard.assertAdministrativeAccessRemains();
    }

    @Test
    void rejectsRemovalOfFinalEffectiveAdministrator() {
        when(guardPort.hasActiveAdministrator()).thenReturn(false);

        assertThatThrownBy(() -> guard.assertAdministrativeAccessRemains())
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("final effective administrator");
    }
}

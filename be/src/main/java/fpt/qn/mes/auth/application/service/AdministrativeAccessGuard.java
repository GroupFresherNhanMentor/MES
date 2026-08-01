package fpt.qn.mes.auth.application.service;

import org.springframework.stereotype.Service;

import fpt.qn.mes.auth.application.port.in.AdministrativeAccessGuardUseCase;
import fpt.qn.mes.auth.application.port.out.RbacMutationGuardPort;
import fpt.qn.mes.common.exception.ConflictException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdministrativeAccessGuard implements AdministrativeAccessGuardUseCase {

    RbacMutationGuardPort guardPort;

    @Override
    public void lock() {
        guardPort.lock();
    }

    @Override
    public void assertAdministrativeAccessRemains() {
        if (!guardPort.hasActiveAdministrator()) {
            throw new ConflictException("Operation would remove the final effective administrator");
        }
    }
}

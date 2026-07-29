package fpt.qn.mes.auth.application.port.out;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.auth.application.security.CredentialAccount;

public interface CredentialQueryPort {

    Optional<CredentialAccount> findByUsername(String username);

    Optional<CredentialAccount> findById(UUID id);
}

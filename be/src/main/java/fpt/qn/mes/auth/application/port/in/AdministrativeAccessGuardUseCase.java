package fpt.qn.mes.auth.application.port.in;

public interface AdministrativeAccessGuardUseCase {

    void lock();

    void assertAdministrativeAccessRemains();
}

package fpt.qn.mes.auth.application.port.out;

public interface RbacMutationGuardPort {

    void lock();

    boolean hasActiveAdministrator();
}

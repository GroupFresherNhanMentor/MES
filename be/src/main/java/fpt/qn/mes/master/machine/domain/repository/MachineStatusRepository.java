package fpt.qn.mes.master.machine.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.machine.domain.entities.MachineStatus;
import fpt.qn.mes.master.machine.domain.repository.criteria.MachineStatusSearchCriteria;

public interface MachineStatusRepository {
    Optional<MachineStatus> findById(UUID id);
    Optional<MachineStatus> findByName(String name);
    boolean existsById(UUID id);
    boolean existsByName(String name);
    MachineStatus save(MachineStatus status);
    PaginationResult<MachineStatus> search(MachineStatusSearchCriteria criteria);
}

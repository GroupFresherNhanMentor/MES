package fpt.qn.mes.master.machine.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.machine.domain.entities.Machine;
import fpt.qn.mes.master.machine.domain.repository.criteria.MachineSearchCriteria;

public interface MachineRepository {
    Optional<Machine> findById(UUID id);
    Machine save(Machine machine);
    Machine update(Machine machine);
    PaginationResult<Machine> search(MachineSearchCriteria criteria);
    boolean existsById(UUID id);
    boolean existsByCode(String code);
}

package fpt.qn.mes.master.machine.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.machine.domain.entities.Machine;

public interface MachineRepository {
    Optional<Machine> findById(UUID id);
    boolean isAvailableForUpdate(UUID id);
    Machine save(Machine machine);
    Machine update(Machine machine);
    void deleteById(UUID id);
    PaginationResult<Machine> findAll(int page, int size);
    PaginationResult<Machine> findAllByStatus(int page, int size, UUID statusId);
    boolean existsByCode(String code);
}

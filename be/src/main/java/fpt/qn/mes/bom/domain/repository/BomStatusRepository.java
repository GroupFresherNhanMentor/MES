package fpt.qn.mes.bom.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.bom.domain.entities.BomStatus;

public interface BomStatusRepository {
    Optional<BomStatus> findById(UUID id);
    Optional<BomStatus> findByName(String name);
    BomStatus save(BomStatus bomStatus);
    boolean existsById(UUID id);
    boolean existsByName(String name);
    List<BomStatus> findAll();
}

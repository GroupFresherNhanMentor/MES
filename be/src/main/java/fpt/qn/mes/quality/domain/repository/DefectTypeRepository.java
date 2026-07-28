package fpt.qn.mes.quality.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.quality.domain.entities.DefectType;

public interface DefectTypeRepository {
    Optional<DefectType> findById(UUID id);
    List<DefectType> findAll();
    DefectType save(DefectType defectType);
}

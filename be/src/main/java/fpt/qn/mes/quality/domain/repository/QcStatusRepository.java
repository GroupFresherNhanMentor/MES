package fpt.qn.mes.quality.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.quality.domain.entities.QcStatus;

public interface QcStatusRepository {
    Optional<QcStatus> findById(UUID id);
    List<QcStatus> findAll();
    QcStatus save(QcStatus status);
}

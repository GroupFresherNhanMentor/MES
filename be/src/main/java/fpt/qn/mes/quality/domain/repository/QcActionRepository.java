package fpt.qn.mes.quality.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.quality.domain.entities.QcAction;

public interface QcActionRepository {
    Optional<QcAction> findById(UUID id);
    List<QcAction> findAll();
    QcAction save(QcAction action);
}

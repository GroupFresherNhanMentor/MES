package fpt.qn.mes.quality.infrastructure.persistence;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.QcActionsRecord;
import fpt.qn.mes.quality.domain.entities.QcAction;

@Component
public class QcActionRecordMapper {

    public QcAction toDomain(QcActionsRecord r) {
        return QcAction.builder()
            .id(r.getId())
            .name(r.getName())
            .description(r.getDescription())
            .build();
    }

    public QcActionsRecord toRecord(QcAction action) {
        QcActionsRecord r = new QcActionsRecord();
        r.setId(action.getId());
        r.setName(action.getName());
        r.setDescription(action.getDescription());
        return r;
    }
}

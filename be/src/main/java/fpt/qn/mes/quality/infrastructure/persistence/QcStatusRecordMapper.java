package fpt.qn.mes.quality.infrastructure.persistence;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.QcStatusesRecord;
import fpt.qn.mes.quality.domain.entities.QcStatus;

@Component
public class QcStatusRecordMapper {

    public QcStatus toDomain(QcStatusesRecord r) {
        return QcStatus.builder()
            .id(r.getId())
            .name(r.getName())
            .description(r.getDescription())
            .build();
    }

    public QcStatusesRecord toRecord(QcStatus status) {
        QcStatusesRecord r = new QcStatusesRecord();
        r.setId(status.getId());
        r.setName(status.getName());
        r.setDescription(status.getDescription());
        return r;
    }
}

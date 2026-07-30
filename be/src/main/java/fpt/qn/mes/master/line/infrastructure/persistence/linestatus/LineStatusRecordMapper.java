package fpt.qn.mes.master.line.infrastructure.persistence.linestatus;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.LineStatusesRecord;
import fpt.qn.mes.master.line.domain.entities.LineStatus;

@Component
public class LineStatusRecordMapper {

    public LineStatus toDomain(LineStatusesRecord r) {
        if (r == null || r.getId() == null) return null;
        return LineStatus.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build();
    }

    public LineStatusesRecord toRecord(LineStatus s) {
        LineStatusesRecord r = new LineStatusesRecord();
        r.setId(s.getId());
        r.setName(s.getName());
        r.setDescription(s.getDescription());
        return r;
    }
}

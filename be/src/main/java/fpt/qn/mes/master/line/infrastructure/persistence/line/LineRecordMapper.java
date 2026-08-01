package fpt.qn.mes.master.line.infrastructure.persistence.line;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.LineStatusesRecord;
import fpt.qn.mes.jooq.tables.records.ProductionLinesRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.master.line.domain.entities.Line;
import fpt.qn.mes.master.line.domain.entities.LineStatus;

@Component
public class LineRecordMapper {

    public Line toDomain(ProductionLinesRecord r, LineStatusesRecord status, UsersRecord creator, UsersRecord updater) {
        if (r == null || r.getId() == null) return null;

        return Line.builder()
                .id(r.getId())
                .code(r.getCode())
                .name(r.getName())
                .lineStatus(status.getId() != null ? LineStatus.builder()
                        .id(status.getId())
                        .name(status.getName())
                        .description(status.getDescription())
                        .build() : null)
                .createdAt(r.getCreatedAt().toInstant())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
                .createdBy(creator.getId() != null
                        ? Line.UserRef.builder()
                            .id(creator.getId())
                            .fullName(creator.getFullName())
                            .username(creator.getUsername())
                            .build()
                        : null)
                .updatedBy(updater.getId() != null
                        ? Line.UserRef.builder()
                            .id(updater.getId())
                            .fullName(updater.getFullName())
                            .username(updater.getUsername())
                            .build()
                        : null)
                .build();
    }

    public ProductionLinesRecord toRecord(Line l) {
        ProductionLinesRecord r = new ProductionLinesRecord();
        r.setId(l.getId());
        r.setCode(l.getCode());
        r.setName(l.getName());
        r.setLineStatusId(l.getLineStatus() != null ? l.getLineStatus().getId() : null);
        r.setCreatedAt(OffsetDateTime.ofInstant(l.getCreatedAt(), ZoneOffset.UTC));
        r.setUpdatedAt(l.getUpdatedAt() != null ? OffsetDateTime.ofInstant(l.getUpdatedAt(), ZoneOffset.UTC) : null);
        r.setCreatedBy(l.getCreatedBy() != null ? l.getCreatedBy().getId() : null);
        r.setUpdatedBy(l.getUpdatedBy() != null ? l.getUpdatedBy().getId() : null);
        return r;
    }
}

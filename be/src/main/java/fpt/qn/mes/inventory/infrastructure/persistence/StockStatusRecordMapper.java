package fpt.qn.mes.inventory.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.inventory.domain.entities.StockStatus;
import fpt.qn.mes.jooq.tables.records.StockStatusesRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;

@Component
public class StockStatusRecordMapper {

    public StockStatus toDomain(StockStatusesRecord r, UsersRecord creator, UsersRecord updater) {
        if (r == null || r.getId() == null) return null;
        return StockStatus.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .createdBy(creator != null && creator.getId() != null
                        ? StockStatus.UserRef.builder()
                                .id(creator.getId())
                                .fullName(creator.getFullName())
                                .username(creator.getUsername())
                                .build()
                        : null)
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
                .updatedBy(updater != null && updater.getId() != null
                        ? StockStatus.UserRef.builder()
                                .id(updater.getId())
                                .fullName(updater.getFullName())
                                .username(updater.getUsername())
                                .build()
                        : null)
                .build();
    }

    public StockStatusesRecord toRecord(StockStatus ss) {
        StockStatusesRecord r = new StockStatusesRecord();
        r.setId(ss.getId());
        r.setName(ss.getName());
        r.setDescription(ss.getDescription());
        r.setCreatedAt(ss.getCreatedAt() != null ? OffsetDateTime.ofInstant(ss.getCreatedAt(), ZoneOffset.UTC) : null);
        r.setUpdatedAt(ss.getUpdatedAt() != null ? OffsetDateTime.ofInstant(ss.getUpdatedAt(), ZoneOffset.UTC) : null);
        r.setCreatedBy(ss.getCreatedBy() != null ? ss.getCreatedBy().getId() : null);
        r.setUpdatedBy(ss.getUpdatedBy() != null ? ss.getUpdatedBy().getId() : null);
        return r;
    }
}

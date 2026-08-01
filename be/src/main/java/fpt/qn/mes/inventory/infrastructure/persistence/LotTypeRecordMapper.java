package fpt.qn.mes.inventory.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.inventory.domain.entities.LotType;
import fpt.qn.mes.jooq.tables.records.LotTypesRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;

@Component
public class LotTypeRecordMapper {

    public LotType toDomain(LotTypesRecord r, UsersRecord creator, UsersRecord updater) {
        if (r == null || r.getId() == null) return null;
        return LotType.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .createdBy(creator != null && creator.getId() != null
                        ? LotType.UserRef.builder()
                                .id(creator.getId())
                                .fullName(creator.getFullName())
                                .username(creator.getUsername())
                                .build()
                        : null)
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
                .updatedBy(updater != null && updater.getId() != null
                        ? LotType.UserRef.builder()
                                .id(updater.getId())
                                .fullName(updater.getFullName())
                                .username(updater.getUsername())
                                .build()
                        : null)
                .build();
    }

    public LotTypesRecord toRecord(LotType lt) {
        LotTypesRecord r = new LotTypesRecord();
        r.setId(lt.getId());
        r.setName(lt.getName());
        r.setDescription(lt.getDescription());
        r.setCreatedAt(lt.getCreatedAt() != null ? OffsetDateTime.ofInstant(lt.getCreatedAt(), ZoneOffset.UTC) : null);
        r.setUpdatedAt(lt.getUpdatedAt() != null ? OffsetDateTime.ofInstant(lt.getUpdatedAt(), ZoneOffset.UTC) : null);
        r.setCreatedBy(lt.getCreatedBy() != null ? lt.getCreatedBy().getId() : null);
        r.setUpdatedBy(lt.getUpdatedBy() != null ? lt.getUpdatedBy().getId() : null);
        return r;
    }
}

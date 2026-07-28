package fpt.qn.mes.master.line.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.ProductionLinesRecord;
import fpt.qn.mes.master.line.domain.entities.ProductionLine;

@Component
public class LineRecordMapper {

    public ProductionLine toDomain(ProductionLinesRecord r) {
        if (r == null) return null;
        return ProductionLine.builder()
                .id(r.getId()).code(r.getCode()).name(r.getName())
                .lineStatusId(r.getLineStatusId())
                .createdAt(r.getCreatedAt().toInstant()).createdBy(r.getCreatedBy())
                .updatedAt(r.getUpdatedAt().toInstant()).updatedBy(r.getUpdatedBy())
                .build();
    }

    public ProductionLinesRecord toRecord(ProductionLine l) {
        ProductionLinesRecord r = new ProductionLinesRecord();
        r.setId(l.getId()); r.setCode(l.getCode()); r.setName(l.getName());
        r.setLineStatusId(l.getLineStatusId());
        r.setCreatedAt(OffsetDateTime.ofInstant(l.getCreatedAt(), ZoneOffset.UTC));
        r.setCreatedBy(l.getCreatedBy());
        r.setUpdatedAt(OffsetDateTime.ofInstant(l.getUpdatedAt(), ZoneOffset.UTC));
        r.setUpdatedBy(l.getUpdatedBy());
        return r;
    }
}

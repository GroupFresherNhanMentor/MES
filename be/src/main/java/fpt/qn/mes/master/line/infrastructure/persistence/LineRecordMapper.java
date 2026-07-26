package fpt.qn.mes.master.line.infrastructure.persistence;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.ProductionLinesRecord;
import fpt.qn.mes.master.line.domain.entities.ProductionLine;

@Component
public class LineRecordMapper {

    public ProductionLine toDomain(ProductionLinesRecord r) {
        return null;
    }

    public ProductionLinesRecord toRecord(ProductionLine l) {
        return null;
    }
}

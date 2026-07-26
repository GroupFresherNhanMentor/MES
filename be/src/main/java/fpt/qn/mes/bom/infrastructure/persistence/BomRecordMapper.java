package fpt.qn.mes.bom.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.entities.BomItem;
import fpt.qn.mes.jooq.tables.records.BomItemsRecord;
import fpt.qn.mes.jooq.tables.records.BomsRecord;

@Component
public class BomRecordMapper {

    public Bom toDomain(BomsRecord r) {
        return null;
    }

    public Bom toDomain(BomsRecord r, List<BomItem> items) {
        return null;
    }

    public BomsRecord toRecord(Bom b) {
        return null;
    }

    public BomItem toDomain(BomItemsRecord r) {
        return null;
    }

    public BomItemsRecord toRecord(BomItem i) {
        return null;
    }
}

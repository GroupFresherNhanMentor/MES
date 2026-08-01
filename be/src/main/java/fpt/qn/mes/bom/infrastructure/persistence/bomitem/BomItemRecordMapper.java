package fpt.qn.mes.bom.infrastructure.persistence.bomitem;

import org.springframework.stereotype.Component;

import fpt.qn.mes.bom.domain.entities.BomItem;
import fpt.qn.mes.jooq.tables.records.BomItemsRecord;
import fpt.qn.mes.jooq.tables.records.ProductsRecord;
import fpt.qn.mes.jooq.tables.records.UnitsOfMeasureRecord;

@Component
public class BomItemRecordMapper {

    public BomItem toDomain(BomItemsRecord r, ProductsRecord material, UnitsOfMeasureRecord unit) {
        if (r == null || r.getId() == null) return null;
        return BomItem.builder()
                .id(r.getId())
                .bomId(r.getBomId())
                .materialProductId(r.getMaterialProductId())
                .materialProductCode(material != null && material.getId() != null ? material.getCode() : null)
                .materialProductName(material != null && material.getId() != null ? material.getName() : null)
                .quantityPerUnit(r.getQuantityPerUnit())
                .unitName(unit != null && unit.getId() != null ? unit.getName() : null)
                .scrapRate(r.getScrapRate())
                .build();
    }

    public BomItemsRecord toRecord(BomItem item) {
        BomItemsRecord r = new BomItemsRecord();
        r.setId(item.getId());
        r.setBomId(item.getBomId());
        r.setMaterialProductId(item.getMaterialProductId());
        r.setQuantityPerUnit(item.getQuantityPerUnit());
        r.setScrapRate(item.getScrapRate());
        return r;
    }
}

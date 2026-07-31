package fpt.qn.mes.bom.infrastructure.persistence;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.entities.BomItem;
import fpt.qn.mes.jooq.tables.records.BomItemsRecord;
import fpt.qn.mes.jooq.tables.records.BomStatusesRecord;
import fpt.qn.mes.jooq.tables.records.BomsRecord;
import fpt.qn.mes.jooq.tables.records.ProductsRecord;
import fpt.qn.mes.jooq.tables.records.UnitsOfMeasureRecord;

@Component
public class BomRecordMapper {

    public Bom toDomain(BomsRecord r) {
        if (r == null) return null;
        return Bom.builder()
                .id(r.getId())
                .finishedProductId(r.getFinishedProductId())
                .version(r.getVersion())
                .bomStatusId(r.getBomStatusId())
                .createdBy(r.getCreatedBy())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .items(new ArrayList<>())
                .build();
    }

    public Bom toDomain(BomsRecord r, List<BomItem> items) {
        if (r == null) return null;
        return Bom.builder()
                .id(r.getId())
                .finishedProductId(r.getFinishedProductId())
                .version(r.getVersion())
                .bomStatusId(r.getBomStatusId())
                .createdBy(r.getCreatedBy())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .items(items != null ? items : new ArrayList<>())
                .build();
    }

    public Bom toDomain(BomsRecord r, List<BomItem> items, ProductsRecord finished, BomStatusesRecord status) {
        if (r == null) return null;
        return Bom.builder()
                .id(r.getId())
                .finishedProductId(r.getFinishedProductId())
                .finishedProductCode(finished != null ? finished.getCode() : null)
                .finishedProductName(finished != null ? finished.getName() : null)
                .version(r.getVersion())
                .bomStatusId(r.getBomStatusId())
                .bomStatusName(status != null ? status.getName() : null)
                .createdBy(r.getCreatedBy())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .items(items != null ? items : new ArrayList<>())
                .build();
    }

    public BomsRecord toRecord(Bom b) {
        if (b == null) return null;
        BomsRecord r = new BomsRecord();
        r.setId(b.getId());
        r.setFinishedProductId(b.getFinishedProductId());
        r.setVersion(b.getVersion());
        r.setBomStatusId(b.getBomStatusId());
        r.setCreatedBy(b.getCreatedBy());
        if (b.getCreatedAt() != null) {
            r.setCreatedAt(b.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        return r;
    }

    public BomItem toDomain(BomItemsRecord r) {
        if (r == null) return null;
        return BomItem.builder()
                .id(r.getId())
                .bomId(r.getBomId())
                .materialProductId(r.getMaterialProductId())
                .quantityPerUnit(r.getQuantityPerUnit())
                .unitId(r.getUnitId())
                .unitName(null)
                .scrapRate(r.getScrapRate())
                .build();
    }

    public BomItem toDomain(BomItemsRecord r, ProductsRecord material, UnitsOfMeasureRecord unit) {
        if (r == null) return null;
        return BomItem.builder()
                .id(r.getId())
                .bomId(r.getBomId())
                .materialProductId(r.getMaterialProductId())
                .materialProductCode(material != null ? material.getCode() : null)
                .materialProductName(material != null ? material.getName() : null)
                .quantityPerUnit(r.getQuantityPerUnit())
                .unitId(r.getUnitId())
                .unitName(unit != null ? unit.getName() : null)
                .scrapRate(r.getScrapRate())
                .build();
    }

    public BomItemsRecord toRecord(BomItem i) {
        if (i == null) return null;
        BomItemsRecord r = new BomItemsRecord();
        r.setId(i.getId());
        r.setBomId(i.getBomId());
        r.setMaterialProductId(i.getMaterialProductId());
        r.setQuantityPerUnit(i.getQuantityPerUnit());
        r.setUnitId(i.getUnitId());
        r.setScrapRate(i.getScrapRate());
        return r;
    }
}

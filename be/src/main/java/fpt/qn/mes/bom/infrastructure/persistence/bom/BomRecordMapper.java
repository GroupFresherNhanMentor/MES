package fpt.qn.mes.bom.infrastructure.persistence.bom;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.entities.BomItem;
import fpt.qn.mes.bom.domain.entities.BomStatus;
import fpt.qn.mes.jooq.tables.records.BomStatusesRecord;
import fpt.qn.mes.jooq.tables.records.BomsRecord;
import fpt.qn.mes.jooq.tables.records.ProductsRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;

@Component
public class BomRecordMapper {

    public Bom toDomain(BomsRecord r, ProductsRecord product, BomStatusesRecord status, UsersRecord creator, List<BomItem> items) {
        if (r == null || r.getId() == null) return null;
        return Bom.builder()
                .id(r.getId())
                .finishedProductId(r.getFinishedProductId())
                .finishedProductCode(product != null && product.getId() != null ? product.getCode() : null)
                .finishedProductName(product != null && product.getId() != null ? product.getName() : null)
                .version(r.getVersion())
                .bomStatus(status != null && status.getId() != null
                        ? BomStatus.builder().id(status.getId()).name(status.getName()).description(status.getDescription()).build()
                        : null)
                .createdBy(creator != null && creator.getId() != null
                        ? Bom.UserRef.builder().id(creator.getId()).fullName(creator.getFullName()).username(creator.getUsername()).build()
                        : null)
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .items(items != null ? items : new ArrayList<>())
                .build();
    }

    public BomsRecord toRecord(Bom bom) {
        BomsRecord r = new BomsRecord();
        r.setId(bom.getId());
        r.setFinishedProductId(bom.getFinishedProductId());
        r.setVersion(bom.getVersion());
        r.setBomStatusId(bom.getBomStatus() != null ? bom.getBomStatus().getId() : null);
        r.setCreatedBy(bom.getCreatedBy() != null ? bom.getCreatedBy().getId() : null);
        if (bom.getCreatedAt() != null) r.setCreatedAt(bom.getCreatedAt().atOffset(ZoneOffset.UTC));
        return r;
    }
}

package fpt.qn.mes.inventory.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.inventory.domain.entities.LotType;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.jooq.tables.records.LotTypesRecord;
import fpt.qn.mes.jooq.tables.records.ProductsRecord;
import fpt.qn.mes.jooq.tables.records.StockLotsRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;

@Component
public class StockLotRecordMapper {

    public StockLot toDomain(StockLotsRecord r, ProductsRecord product, LotTypesRecord lotType,
            UsersRecord creator, UsersRecord updater) {
        if (r == null || r.getId() == null) return null;
        StockLot.ProductRef productRef = product != null && product.getId() != null
                ? StockLot.ProductRef.builder()
                        .id(product.getId())
                        .code(product.getCode())
                        .name(product.getName())
                        .build()
                : StockLot.ProductRef.builder().id(r.getProductId()).build();
        LotType lotTypeRef = lotType != null && lotType.getId() != null
                ? LotType.builder().id(lotType.getId()).name(lotType.getName()).build()
                : null;
        return StockLot.builder()
                .id(r.getId())
                .lotNumber(r.getLotNumber())
                .product(productRef)
                .lotType(lotTypeRef)
                .expiryDate(r.getExpiryDate())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .createdBy(creator != null && creator.getId() != null
                        ? StockLot.UserRef.builder()
                                .id(creator.getId())
                                .fullName(creator.getFullName())
                                .username(creator.getUsername())
                                .build()
                        : null)
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
                .updatedBy(updater != null && updater.getId() != null
                        ? StockLot.UserRef.builder()
                                .id(updater.getId())
                                .fullName(updater.getFullName())
                                .username(updater.getUsername())
                                .build()
                        : null)
                .build();
    }

    public StockLotsRecord toRecord(StockLot l) {
        StockLotsRecord r = new StockLotsRecord();
        r.setId(l.getId());
        r.setLotNumber(l.getLotNumber());
        r.setProductId(l.getProductId());
        r.setLotTypeId(l.getLotTypeId());
        r.setExpiryDate(l.getExpiryDate());
        if (l.getCreatedAt() != null) r.setCreatedAt(OffsetDateTime.ofInstant(l.getCreatedAt(), ZoneOffset.UTC));
        if (l.getUpdatedAt() != null) r.setUpdatedAt(OffsetDateTime.ofInstant(l.getUpdatedAt(), ZoneOffset.UTC));
        r.setCreatedBy(l.getCreatedBy() != null ? l.getCreatedBy().getId() : null);
        r.setUpdatedBy(l.getUpdatedBy() != null ? l.getUpdatedBy().getId() : null);
        return r;
    }
}

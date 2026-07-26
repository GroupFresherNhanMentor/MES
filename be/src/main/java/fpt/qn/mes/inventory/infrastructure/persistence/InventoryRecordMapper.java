package fpt.qn.mes.inventory.infrastructure.persistence;

import org.springframework.stereotype.Component;

import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.jooq.tables.records.StockBalancesRecord;
import fpt.qn.mes.jooq.tables.records.StockLotsRecord;
import fpt.qn.mes.jooq.tables.records.StockMovementsRecord;

@Component
public class InventoryRecordMapper {

    public StockLot toDomain(StockLotsRecord r) {
        return null;
    }

    public StockLotsRecord toRecord(StockLot l) {
        return null;
    }

    public StockMovement toDomain(StockMovementsRecord r) {
        return null;
    }

    public StockMovementsRecord toRecord(StockMovement m) {
        return null;
    }

    public StockBalance toDomain(StockBalancesRecord r) {
        return null;
    }
}

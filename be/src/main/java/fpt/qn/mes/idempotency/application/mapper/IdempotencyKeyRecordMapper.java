package fpt.qn.mes.idempotency.application.mapper;

import org.mapstruct.Mapper;

import fpt.qn.mes.idempotency.domain.entities.IdempotencyKey;
import fpt.qn.mes.jooq.tables.records.IdempotencyKeysRecord;

@Mapper(componentModel = "spring")
public interface IdempotencyKeyRecordMapper {

    IdempotencyKey toDomain(IdempotencyKeysRecord record);

    IdempotencyKeysRecord toRecord(IdempotencyKey entity);
}

package fpt.qn.mes.idempotency.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.IDEMPOTENCY_KEYS;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.idempotency.application.mapper.IdempotencyKeyRecordMapper;
import fpt.qn.mes.idempotency.domain.entities.IdempotencyKey;
import fpt.qn.mes.idempotency.domain.repository.IdempotencyKeyRepository;
import fpt.qn.mes.jooq.tables.records.IdempotencyKeysRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdempotencyKeyPersistenceAdapter extends BaseRepository<IdempotencyKeysRecord> implements IdempotencyKeyRepository {

    IdempotencyKeyRecordMapper mapper;

    public IdempotencyKeyPersistenceAdapter(DSLContext ctx, IdempotencyKeyRecordMapper mapper) {
        super(ctx, IDEMPOTENCY_KEYS);
        this.mapper = mapper;
    }

    @Override
    public Optional<IdempotencyKey> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return fetchById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<IdempotencyKey> findByKeyAndUserOrClient(String key, UUID userId, String clientIdentifier) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        var condition = IDEMPOTENCY_KEYS.KEY.eq(key);
        if (userId != null) {
            condition = condition.and(IDEMPOTENCY_KEYS.USER_ID.eq(userId));
        } else {
            condition = condition.and(IDEMPOTENCY_KEYS.USER_ID.isNull())
                    .and(IDEMPOTENCY_KEYS.CLIENT_IDENTIFIER.eq(clientIdentifier));
        }

        return ctx.selectFrom(IDEMPOTENCY_KEYS)
                .where(condition)
                .fetchOptional()
                .map(mapper::toDomain);
    }

    @Override
    public IdempotencyKey save(IdempotencyKey entity) {
        IdempotencyKeysRecord record = mapper.toRecord(entity);
        if (record.getId() == null) {
            record.setId(UUID.randomUUID());
        }
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        }
        record.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        ctx.attach(record);
        record.store();
        return mapper.toDomain(record);
    }

    @Override
    public void updateResponse(UUID id, int statusCode, String responseBody) {
        ctx.update(IDEMPOTENCY_KEYS)
                .set(IDEMPOTENCY_KEYS.STATUS_CODE, statusCode)
                .set(IDEMPOTENCY_KEYS.RESPONSE_BODY, responseBody)
                .set(IDEMPOTENCY_KEYS.UPDATED_AT, OffsetDateTime.now(ZoneOffset.UTC))
                .where(IDEMPOTENCY_KEYS.ID.eq(id))
                .execute();
    }

    @Override
    public void deleteById(UUID id) {
        hardDeleteById(id);
    }

    @Override
    public int deleteRecordsOlderThanDays(int days) {
        OffsetDateTime cutoff = OffsetDateTime.now(ZoneOffset.UTC).minus(days, ChronoUnit.DAYS);
        return ctx.deleteFrom(IDEMPOTENCY_KEYS)
                .where(IDEMPOTENCY_KEYS.CREATED_AT.lt(cutoff))
                .execute();
    }
}

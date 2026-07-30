package fpt.qn.mes.common.idempotency.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.idempotency.domain.entities.IdempotencyKey;
import fpt.qn.mes.common.repository.BaseDomainRepository;

public interface IdempotencyKeyRepository extends BaseDomainRepository<IdempotencyKey, UUID> {

    Optional<IdempotencyKey> findByKeyAndUserOrClient(String key, UUID userId, String clientIdentifier);

    IdempotencyKey save(IdempotencyKey entity);

    void updateResponse(UUID id, int statusCode, String responseBody);

    void deleteById(UUID id);

    int deleteRecordsOlderThanDays(int days);
}

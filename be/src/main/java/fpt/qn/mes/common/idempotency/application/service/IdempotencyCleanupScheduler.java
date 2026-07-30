package fpt.qn.mes.common.idempotency.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fpt.qn.mes.common.idempotency.domain.repository.IdempotencyKeyRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdempotencyCleanupScheduler {

    IdempotencyKeyRepository repository;

    /**
     * Daily scheduled job at midnight to purge idempotency records created > 1 day ago.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void purgeExpiredKeys() {
        log.info("Starting scheduled cleanup of idempotency keys older than 1 day...");
        int deletedCount = repository.deleteRecordsOlderThanDays(1);
        log.info("Finished idempotency keys cleanup. Purged {} record(s).", deletedCount);
    }
}

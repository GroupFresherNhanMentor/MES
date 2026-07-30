package fpt.qn.mes.common.idempotency;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fpt.qn.mes.AbstractIntegrationTest;
import fpt.qn.mes.common.idempotency.application.service.IdempotencyService;
import fpt.qn.mes.common.idempotency.application.service.IdempotencyService.ClaimResult;
import fpt.qn.mes.common.idempotency.domain.repository.IdempotencyKeyRepository;

class IdempotencyFilterIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private IdempotencyKeyRepository repository;

    private UUID userId;
    private String key;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        key = "concurrency-key-" + UUID.randomUUID();
    }

    @Test
    @DisplayName("Concurrent claim requests with same idempotency key must result in exactly 1 success claim and 1 conflict exception")
    void concurrentClaims_SameKey_ExactlyOneSucceeds() throws InterruptedException {
        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        byte[] body = "{\"item\":\"widget\"}".getBytes();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    ClaimResult result = idempotencyService.claimOrRetrieve(
                            key, userId, "127.0.0.1:client", "/api/test", "POST", body
                    );
                    if (result.isNewClaim()) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("currently being processed")) {
                        conflictCount.incrementAndGet();
                    }
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown(); // Release all threads simultaneously
        doneLatch.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(1);
    }
}

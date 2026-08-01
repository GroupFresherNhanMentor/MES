package fpt.qn.mes.auth.support;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrentRequestRunner {

    public List<Integer> run(
            int concurrency,
            Callable<Integer> request,
            Duration timeout) throws InterruptedException {
        java.util.ArrayList<Callable<Integer>> requests = new java.util.ArrayList<>();
        for (int index = 0; index < concurrency; index++) {
            requests.add(request);
        }
        return run(requests, timeout);
    }

    public List<Integer> run(
            List<? extends Callable<Integer>> requests,
            Duration timeout) throws InterruptedException {
        int concurrency = requests.size();
        CountDownLatch ready = new CountDownLatch(concurrency);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(concurrency);
        List<Integer> statuses = new CopyOnWriteArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);

        for (Callable<Integer> request : requests) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    statuses.add(request.call());
                } catch (Exception exception) {
                    statuses.add(500);
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        start.countDown();
        boolean completed = done.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        executor.shutdownNow();
        if (!completed) {
            throw new IllegalStateException("Concurrent requests did not finish before timeout");
        }
        return List.copyOf(statuses);
    }
}

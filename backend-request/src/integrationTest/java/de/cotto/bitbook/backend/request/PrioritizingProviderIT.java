package de.cotto.bitbook.backend.request;

import de.cotto.bitbook.backend.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(PrioritizingProviderIT.TestConfig.class)
class PrioritizingProviderIT {
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    @Autowired
    private PrioritizingProviderForTest prioritizingProvider;

    @Autowired
    private List<ProviderForTest> providers;

    @Autowired
    private List<RequestWorker<?, ?>> requestWorkers;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    private ProviderForTest provider1;
    private ProviderForTest provider2;

    @BeforeEach
    void setUp() {
        PrioritizingProviderForTest.WORK_ON_REQUESTS_IS_DISABLED.set(true);
        prioritizingProvider.requestQueue.clear();
        requestWorkers.forEach(RequestWorker::resetScores);
        providers.forEach(provider -> provider.seenKeys.clear());
        provider1 = providers.get(0);
        provider2 = providers.get(1);
    }

    @Test
    void many_pending_requests() throws ExecutionException, InterruptedException {
        ForkJoinPool forkJoinPool = new ForkJoinPool(200);
        int max = 500;
        executor.execute(() -> {
                await().atMost(10, SECONDS).until(() -> prioritizingProvider.requestQueue.size() == max);
                workOnScheduledTasks();
            }
        );

        Set<Integer> results = forkJoinPool.submit(() ->
                IntStream.range(0, max).parallel()
                        .mapToObj(i -> new PrioritizedRequest<String, Integer>(Integer.toString(i), STANDARD))
                        .map(request -> prioritizingProvider.getForRequestBlocking(request))
                        .map(Optional::orElseThrow)
                        .collect(Collectors.toSet())
        ).get();

        assertThat(results).hasSize(3);
    }

    @Test
    void requests_are_ordered_by_priority() {
        provider2.disabled = true;
        executor.execute(
                () -> {
                    prioritizingProvider.getForRequestBlocking(new PrioritizedRequest<>("lowest", LOWEST));
                    prioritizingProvider.getForRequestBlocking(new PrioritizedRequest<>("standard", STANDARD));
                }
        );
        await().atMost(1, SECONDS).until(() -> prioritizingProvider.requestQueue.size() == 2);
        workOnScheduledTasks();
        await().atMost(2, SECONDS).untilAsserted(
                () -> assertThat(provider1.seenKeys).containsExactly("standard", "lowest")
        );
    }

    @Test
    void providers_are_used_in_order() {
        executor.execute(
                () -> prioritizingProvider.getForRequestBlocking(new PrioritizedRequest<>("xxx", STANDARD))
        );
        workOnScheduledTasks();
        await().atMost(1, SECONDS).untilAsserted(
                () -> {
                    assertThat(provider1.seenKeys).containsExactly("xxx");
                    assertThat(provider2.seenKeys).isEmpty();
                }
        );
    }

    @Test
    void providers_are_ordered_by_priority() {
        assertThat(provider1.order).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
        assertThat(provider2.order).isEqualTo(Ordered.LOWEST_PRECEDENCE);
    }

    @Test
    void lowest_priority_requests_are_queued_and_immediately_return() {
        Optional<Integer> result =
                prioritizingProvider.getForRequestBlocking(new PrioritizedRequest<>("lowest", LOWEST));
        assertThat(prioritizingProvider.requestQueue).hasSize(1);
        assertThat(result).isEmpty();
    }

    private static class ProviderForTest implements Provider<String, Integer>, Ordered {
        private final List<String> seenKeys = new ArrayList<>();
        private final int order;
        private boolean disabled;

        private ProviderForTest(int order) {
            this.order = order;
        }

        @Override
        public String getName() {
            return "x";
        }

        @Override
        public Optional<Integer> get(String key) {
            if (disabled) {
                throw new IllegalStateException();
            }
            seenKeys.add(key);
            if ("sleep".equals(key)) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            return Optional.of(key.length());
        }

        @Override
        public int getOrder() {
            return order;
        }
    }

    private void workOnScheduledTasks() {
        PrioritizingProviderForTest.WORK_ON_REQUESTS_IS_DISABLED.set(false);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public PrioritizingProviderForTest prioritizingProviderForTest(List<ProviderForTest> providers) {
            return new PrioritizingProviderForTest(providers);
        }

        @Bean
        public ProviderForTest providerWithLowPriority() {
            return new ProviderForTest(Ordered.LOWEST_PRECEDENCE);
        }

        @Bean
        public ProviderForTest providerWithHighPriority() {
            return new ProviderForTest(Ordered.HIGHEST_PRECEDENCE);
        }
    }

    private static class PrioritizingProviderForTest extends PrioritizingProvider<String, Integer> {
        private static final AtomicBoolean WORK_ON_REQUESTS_IS_DISABLED = new AtomicBoolean(true);

        private PrioritizingProviderForTest(List<ProviderForTest> providers) {
            super(WORK_ON_REQUESTS_IS_DISABLED, providers, "");
        }
    }

    @Nested
    class ReusesRequest {
        private static final String KEY = "sleep";

        @Nullable
        private Integer resultConsumerLowest;

        @Nullable
        private Integer resultConsumerStandard;

        private Optional<Integer> requestInParallel() throws InterruptedException {
            // Submit two requests. Make sure second request is added after first is started.
            AtomicBoolean standardRequestDone = new AtomicBoolean(false);
            AtomicReference<Optional<Integer>> resultStandardRequest = new AtomicReference<>();
            taskExecutor.execute(() -> {
                PrioritizedRequest<String, Integer> requestStandard =
                        new PrioritizedRequest<>(KEY, STANDARD, this::consumerStandard);
                resultStandardRequest.set(prioritizingProvider.getForRequestBlocking(requestStandard));
                standardRequestDone.set(true);
            });
            workOnScheduledTasks();
            await().until(() -> !provider1.seenKeys.isEmpty() || !provider2.seenKeys.isEmpty());
            prioritizingProvider.getForRequestBlocking(new PrioritizedRequest<>(KEY, LOWEST, this::consumerLowest));

            // Wait until both are done so that we can assert only one (the first) hits the network.
            await().until(standardRequestDone::get);
            waitForExecutorToFinish(500);

            return Objects.requireNonNull(resultStandardRequest.get());
        }

        @Test
        void reuses_already_running_request() throws Exception {
            Optional<Integer> result = requestInParallel();
            List<String> seenKeys = new ArrayList<>(provider1.seenKeys);
            seenKeys.addAll(provider2.seenKeys);
            assertThat(seenKeys).hasSize(1);
            assertThat(result).contains(KEY.length());
        }

        @Test
        void reusing_request_also_delivers_result_to_result_consumers() throws Exception {
            requestInParallel();
            assertThat(resultConsumerLowest).isEqualTo(KEY.length());
            assertThat(resultConsumerStandard).isEqualTo(KEY.length());
        }

        private void consumerLowest(Integer result) {
            this.resultConsumerLowest = result;
        }

        private void consumerStandard(Integer result) {
            this.resultConsumerStandard = result;
        }

        @SuppressWarnings("SameParameterValue")
        private void waitForExecutorToFinish(int milliseconds) throws InterruptedException {
            //noinspection ResultOfMethodCallIgnored
            taskExecutor.getThreadPoolExecutor().awaitTermination(milliseconds, TimeUnit.MILLISECONDS);
        }
    }
}
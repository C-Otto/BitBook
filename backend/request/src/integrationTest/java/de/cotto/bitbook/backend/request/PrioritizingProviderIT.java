package de.cotto.bitbook.backend.request;

import de.cotto.bitbook.backend.model.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private TestablePrioritizingProviderFor prioritizingProvider;

    @Autowired
    private List<TestableProvider> providers;

    @Autowired
    private List<RequestWorker<?, ?>> requestWorkers;

    private TestableProvider provider1;
    private TestableProvider provider2;

    @BeforeEach
    void setUp() {
        prioritizingProvider.requestQueue.clear();
        requestWorkers.forEach(RequestWorker::resetScores);
        providers.forEach(provider -> provider.seenKeys.clear());
        provider1 = providers.get(0);
        provider2 = providers.get(1);
        provider2.disabled = true;
    }

    @Test
    void requests_are_ordered_by_priority() {
        synchronized (prioritizingProvider.requestQueue) {
            addToQueue(new PrioritizedRequest<>("lowest", LOWEST));
            addToQueue(new PrioritizedRequest<>("standard", STANDARD));
        }
        assertThat(prioritizingProvider.requestQueue).hasSize(2);
        await().atMost(2, SECONDS).untilAsserted(
                () -> assertThat(provider1.seenKeys).containsExactly("standard", "lowest")
        );
    }

    @Test
    void providers_are_used_in_order() {
        provider2.disabled = false;
        executor.execute(
                () -> prioritizingProvider.getForRequestBlocking(new PrioritizedRequest<>("xxx", STANDARD))
        );
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

    private static class TestableProvider implements Provider<String, Integer>, Ordered {
        private final List<String> seenKeys = new ArrayList<>();
        private final int order;
        private boolean disabled;

        private TestableProvider(int order) {
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

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestablePrioritizingProviderFor prioritizingProviderForTest(List<TestableProvider> providers) {
            return new TestablePrioritizingProviderFor(providers);
        }

        @Bean
        public TestableProvider providerWithLowPriority() {
            return new TestableProvider(Ordered.LOWEST_PRECEDENCE);
        }

        @Bean
        public TestableProvider providerWithHighPriority() {
            return new TestableProvider(Ordered.HIGHEST_PRECEDENCE);
        }
    }

    private static class TestablePrioritizingProviderFor extends PrioritizingProvider<String, Integer> {
        private TestablePrioritizingProviderFor(List<TestableProvider> providers) {
            super(providers, "");
        }
    }

    private void addToQueue(PrioritizedRequest<String, Integer> request) {
        prioritizingProvider.requestQueue.offer(request.getWithResultFuture());
    }
}

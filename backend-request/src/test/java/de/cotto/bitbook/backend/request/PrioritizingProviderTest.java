package de.cotto.bitbook.backend.request;

import de.cotto.bitbook.backend.Provider;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Timeout(2)
class PrioritizingProviderTest {
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private TestableProvider provider1;
    private TestableProvider provider2;
    private TestablePrioritizingProvider prioritizingProvider;

    @BeforeEach
    void setUp() {
        provider1 = spy(new TestableProvider());
        provider2 = spy(new TestableProvider());
        prioritizingProvider = new TestablePrioritizingProvider(List.of(provider1, provider2));
    }

    @Test
    void getForRequest() {
        workOnExpectedRequests(1);
        Optional<Integer> result = prioritizingProvider.getForRequestBlocking(request("xxxxx", STANDARD));
        assertThat(result).contains(5);
    }

    @Test
    void all_providers_fail() {
        workOnExpectedRequests(1);
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenThrow(mock(CallNotPermittedException.class));
        Optional<Integer> result = prioritizingProvider.getForRequestBlocking(request("xyz", STANDARD));
        assertThat(result).isEmpty();
    }

    @Test
    void all_providers_fail_removes_low_priority_requests_from_queue() {
        workOnExpectedRequests(2);
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenThrow(mock(CallNotPermittedException.class));
        prioritizingProvider.getForRequestBlocking(request("yyy", LOWEST));

        Optional<Integer> result = prioritizingProvider.getForRequestBlocking(request("...", STANDARD));

        assertThat(result).isEmpty();
        assertThat(prioritizingProvider.requestQueue).isEmpty();
    }

    @Test
    void all_providers_fail_retains_standard_priority_request() {
        workOnExpectedRequests(2);
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenThrow(mock(CallNotPermittedException.class));
        executor.execute(() -> {
            await().atMost(1, SECONDS).until(() -> prioritizingProvider.requestQueue.size() == 1);
            prioritizingProvider.getForRequestBlocking(request("anotherRequest", STANDARD));
        });

        Optional<Integer> result = prioritizingProvider.getForRequestBlocking(request("request", STANDARD));
        assertThat(result).isEmpty();

        await().atMost(1, SECONDS).untilAsserted(() -> {
                verify(provider1).get("request");
                verify(provider1).get("anotherRequest");
            }
        );
    }

    @Test
    void merges_requests_with_same_key_and_priority() {
        when(provider2.get(any())).thenThrow(mock(CallNotPermittedException.class));
        prioritizingProvider.getForRequestBlocking(request("a", LOWEST));
        prioritizingProvider.getForRequestBlocking(request("bb", LOWEST));
        prioritizingProvider.getForRequestBlocking(request("ccc", LOWEST));
        prioritizingProvider.getForRequestBlocking(request("a", LOWEST));
        prioritizingProvider.workOnRequests();

        await().atMost(1, SECONDS).until(prioritizingProvider.requestQueue::isEmpty);
        assertThat(provider1.seenKeys).hasSize(3);
    }

    @Test
    void does_not_merge_requests_with_different_keys() {
        when(provider2.get(any())).thenThrow(mock(CallNotPermittedException.class));
        workOnExpectedRequests(2);
        executor.execute(() -> prioritizingProvider.getForRequestBlocking(request("a", STANDARD)));
        executor.execute(() -> prioritizingProvider.getForRequestBlocking(request("bb", STANDARD)));
        await().atMost(1, SECONDS).untilAsserted(() -> {
                synchronized (provider1.seenKeys) {
                    assertThat(provider1.seenKeys).hasSize(2);
                }
            }
        );
    }

    @Test
    void merges_low_priority_requests_into_standard_priority_request() {
        when(provider2.get(any())).thenThrow(mock(CallNotPermittedException.class));
        executor.execute(() -> prioritizingProvider.getForRequestBlocking(request("a", STANDARD)));
        executor.execute(() -> {
            await().until(() -> prioritizingProvider.requestQueue.size() > 0);
            prioritizingProvider.getForRequestBlocking(request("a", LOWEST));
            prioritizingProvider.workOnRequests();
        });
        await().atMost(1, SECONDS).untilAsserted(() ->
            assertThat(provider1.seenKeys).hasSize(1)
        );
    }

    @Test
    void replaces_low_priority_requests_with_standard_priority_request() {
        when(provider2.get(any())).thenThrow(mock(CallNotPermittedException.class));
        executor.execute(() -> {
            await().until(() -> prioritizingProvider.requestQueue.stream()
                    .anyMatch(request -> request.getPriority() == STANDARD));
            prioritizingProvider.workOnRequests();
        });
        executor.execute(() -> prioritizingProvider.getForRequestBlocking(request("other1", LOWEST)));
        executor.execute(() -> prioritizingProvider.getForRequestBlocking(request("other2", LOWEST)));
        executor.execute(() -> prioritizingProvider.getForRequestBlocking(request("a", LOWEST)));
        executor.execute(() -> {
            await().until(() -> prioritizingProvider.requestQueue.size() == 3);
            prioritizingProvider.getForRequestBlocking(request("a", STANDARD));
        });
        await().atMost(1, SECONDS).untilAsserted(() ->
                assertThat(provider1.seenKeys).startsWith("a")
        );
        await().pollDelay(200, MILLISECONDS).atMost(1, SECONDS).untilAsserted(() ->
                assertThat(provider1.seenKeys).hasSize(3)
        );
    }

    @Test
    void merges_with_already_running_request_with_same_key() {
        String key = "wait";
        ResultFuture<Integer> result1 = prioritizingProvider.getForRequest(request(key, LOWEST));
        workOnExpectedRequests(1);
        await().atMost(1, SECONDS).until(() -> !prioritizingProvider.runningRequests.isEmpty());
        ResultFuture<Integer> result2 =
                prioritizingProvider.getForRequest(request(key, STANDARD));
        assertThat(result1.getResult()).contains(4);
        assertThat(result2.getResult()).contains(4);
    }

    @Test
    void does_not_merge_with_already_running_request_with_different_key() throws Exception {
        ResultFuture<Integer> result1 = prioritizingProvider.getForRequest(request("wait", LOWEST));
        workOnExpectedRequests(1);
        await().atMost(1, SECONDS).until(() -> !prioritizingProvider.runningRequests.isEmpty());

        ResultFuture<Integer> result2 =
                prioritizingProvider.getForRequest(request("something-else", LOWEST));

        assertThat(result1.getFuture().get(1, SECONDS)).isEqualTo(4);
        assertThat(result2.getFuture().get(1, SECONDS)).isNotEqualTo(4);
    }

    @Test
    void worksOnQueue() {
        when(provider2.get(any())).thenThrow(mock(CallNotPermittedException.class));
        prioritizingProvider.getForRequestBlocking(request("lowest", LOWEST));
        executor.execute(() -> prioritizingProvider.getForRequestBlocking(request("standard", STANDARD)));

        workOnExpectedRequests(2);
        await().atMost(1, SECONDS).untilAsserted(() -> {
                synchronized (provider1.seenKeys) {
                    assertThat(provider1.seenKeys).containsExactly("standard", "lowest");
                }
            }
        );
    }

    @Test
    void getQueueByPriority_values() {
        executor.execute(() -> prioritizingProvider.getForRequestBlocking(new PrioritizedRequest<>("x", STANDARD)));
        executor.execute(() -> prioritizingProvider.getForRequestBlocking(new PrioritizedRequest<>("y", STANDARD)));
        await().atMost(1, SECONDS).untilAsserted(
                () -> assertThat(prioritizingProvider.getQueueByPriority().get(STANDARD)).hasSize(2)
        );
    }

    @Test
    void getQueueByPriority_keys() {
        executor.execute(() -> prioritizingProvider.getForRequestBlocking(new PrioritizedRequest<>("x", STANDARD)));
        executor.execute(() -> prioritizingProvider.getForRequestBlocking(new PrioritizedRequest<>("y", LOWEST)));
        await().atMost(1, SECONDS).untilAsserted(
                () -> assertThat(prioritizingProvider.getQueueByPriority()).containsOnlyKeys(STANDARD, LOWEST)
        );
    }

    private PrioritizedRequest<String, Integer> request(String key, RequestPriority priority) {
        return new PrioritizedRequest<>(key, priority);
    }

    private void workOnExpectedRequests(int expectedRequests) {
        executor.execute(() -> {
            await().until(() -> prioritizingProvider.requestQueue.size() == expectedRequests);
            prioritizingProvider.workOnRequests();
        });
    }

    @Test
    void getProvidedResultName() {
        assertThat(prioritizingProvider.getProvidedResultName()).isEqualTo("xxx");
    }

    private static class TestablePrioritizingProvider extends PrioritizingProvider<String, Integer> {
        private TestablePrioritizingProvider(List<Provider<String, Integer>> providers) {
            super(providers, "xxx");
        }
    }

}
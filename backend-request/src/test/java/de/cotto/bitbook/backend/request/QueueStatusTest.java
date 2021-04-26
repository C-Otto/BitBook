package de.cotto.bitbook.backend.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueueStatusTest {
    private TestableQueueStatus queueStatus;

    @Mock
    private PrioritizingProvider<String, String> provider1;

    @Mock
    private PrioritizingProvider<Integer, Object> provider2;

    @BeforeEach
    void setUp() {
        queueStatus = new TestableQueueStatus(List.of(provider1, provider2));
    }

    @Test
    void logQueueStatus_prints_for_each_provider() {
        provider1HasOneRequest(STANDARD);
        provider2HasOneRequest(STANDARD);
        queueStatus.logQueueStatus();
        assertThat(queueStatus.providers).containsExactly(provider1, provider2);
    }

    @Test
    void logQueueStatus_counts_requests() {
        provider1HasTwoStandardRequests();
        provider2HasOneStandardAndOneLowestRequest();
        queueStatus.logQueueStatus();
        assertThat(queueStatus.sizes).containsExactly(2, 1);
        assertThat(queueStatus.requestPriorities).containsExactly(STANDARD, STANDARD);
    }

    @Test
    void logQueueStatus_ignores_lowest_priority_requests() {
        provider1HasOneRequest(LOWEST);
        provider2HasOneRequest(LOWEST);
        queueStatus.logQueueStatus();
        assertThat(queueStatus.sizes).isEmpty();
    }

    @Test
    void logQueueStatus_no_difference_provided_for_first_invocation() {
        provider1HasOneRequest(STANDARD);
        provider2HasOneRequest(STANDARD);
        queueStatus.logQueueStatus();
        assertThat(queueStatus.formattedDifferences).containsExactly("", "");
    }

    @Test
    void logQueueStatus_includes_difference_to_previous_log_positive_and_zero() {
        provider1HasTwoStandardRequests();
        provider2HasOneStandardAndOneLowestRequest();
        queueStatus.logQueueStatus();
        queueStatus.reset();

        provider1HasTwoStandardRequests();
        provider2HasTwoStandardRequests();
        queueStatus.logQueueStatus();
        assertThat(queueStatus.formattedDifferences).containsExactly(" (+0)", " (+1)");
    }

    @Test
    void logQueueStatus_includes_difference_to_previous_log_negative() {
        provider1HasTwoStandardRequests();
        queueStatus.logQueueStatus();
        queueStatus.reset();

        when(provider1.getQueueByPriority()).thenReturn(Map.of(
                STANDARD, List.of(new PrioritizedRequestWithResult<>("d", STANDARD))
        ));
        queueStatus.logQueueStatus();
        assertThat(queueStatus.formattedDifferences).containsExactly(" (-1)");
    }

    private void provider1HasTwoStandardRequests() {
        when(provider1.getQueueByPriority()).thenReturn(Map.of(
                STANDARD, List.of(
                        new PrioritizedRequestWithResult<>("b", STANDARD),
                        new PrioritizedRequestWithResult<>("c", STANDARD)
                )
        ));
    }

    private void provider1HasOneRequest(RequestPriority priority) {
        when(provider1.getQueueByPriority()).thenReturn(
                Map.of(priority, List.of(new PrioritizedRequestWithResult<>("a", priority)))
        );
    }

    private void provider2HasOneRequest(RequestPriority priority) {
        when(provider2.getQueueByPriority()).thenReturn(
                Map.of(priority, List.of(new PrioritizedRequestWithResult<>(1, priority)))
        );
    }

    private void provider2HasOneStandardAndOneLowestRequest() {
        when(provider2.getQueueByPriority()).thenReturn(Map.of(
                STANDARD, List.of(new PrioritizedRequestWithResult<>(1, STANDARD)),
                LOWEST, List.of(new PrioritizedRequestWithResult<>(2, LOWEST))
        ));
    }

    private void provider2HasTwoStandardRequests() {
        when(provider2.getQueueByPriority()).thenReturn(Map.of(
                STANDARD, List.of(
                        new PrioritizedRequestWithResult<>(1, STANDARD),
                        new PrioritizedRequestWithResult<>(2, STANDARD)
                )
        ));
    }

    private static class TestableQueueStatus extends QueueStatus {
        private final List<PrioritizingProvider<?, ?>> providers = new ArrayList<>();
        private final List<RequestPriority> requestPriorities = new ArrayList<>();
        private final List<Integer> sizes = new ArrayList<>();
        private final List<String> formattedDifferences = new ArrayList<>();

        public TestableQueueStatus(List<PrioritizingProvider<?, ?>> providers) {
            super(providers);
        }

        @Override
        protected void log(
                PrioritizingProvider<?, ?> provider,
                RequestPriority requestPriority,
                int outstanding,
                String formattedDifference
        ) {
            providers.add(provider);
            requestPriorities.add(requestPriority);
            sizes.add(outstanding);
            formattedDifferences.add(formattedDifference);
        }

        void reset() {
            providers.clear();
            requestPriorities.clear();
            sizes.clear();
            formattedDifferences.clear();
        }
    }
}
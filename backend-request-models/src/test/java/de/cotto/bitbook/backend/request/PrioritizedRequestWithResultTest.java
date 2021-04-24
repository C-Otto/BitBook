package de.cotto.bitbook.backend.request;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;

class PrioritizedRequestWithResultTest {

    @Test
    void compareTo_standard_before_low() {
        PrioritizedRequestWithResult<String, Object> lowest = new PrioritizedRequestWithResult<>("a", LOWEST);
        PrioritizedRequestWithResult<String, Object> standard = new PrioritizedRequestWithResult<>("a", STANDARD);
        assertThat(lowest.compareTo(standard)).isGreaterThan(0);
    }

    @Test
    void compareTo_key_as_second_order_criteria() {
        PrioritizedRequestWithResult<String, Object> instanceA = new PrioritizedRequestWithResult<>("a", STANDARD);
        PrioritizedRequestWithResult<String, Object> instanceB = new PrioritizedRequestWithResult<>("b", STANDARD);
        assertThat(instanceA.compareTo(instanceB)).isNotEqualTo(0);
    }

    @Test
    void compareTo_two_instances() {
        PrioritizedRequestWithResult<String, Object> instance1 = new PrioritizedRequestWithResult<>("a", STANDARD);
        PrioritizedRequestWithResult<String, Object> instance2 = new PrioritizedRequestWithResult<>("a", STANDARD);
        int oneToTwo = (int) Math.signum(instance1.compareTo(instance2));
        int twoToOne = (int) Math.signum(instance2.compareTo(instance1));
        assertThat(oneToTwo).isNotEqualTo(0).isEqualTo(-twoToOne);
    }

    @Test
    void compareTo_same_instance() {
        PrioritizedRequestWithResult<String, Object> instance = new PrioritizedRequestWithResult<>("a", STANDARD);
        @SuppressWarnings("UnnecessaryLocalVariable")
        PrioritizedRequestWithResult<String, Object> instance2 = instance;
        assertThat(instance.compareTo(instance2)).isEqualTo(0);
    }

    @Test
    void getRequestPriority() {
        assertThat(new PrioritizedRequestWithResult<>("a", LOWEST).getPriority()).isEqualTo(LOWEST);
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    class CreateMergedReplacement {
        private static final String KEY = "a";
        private static final String EXPECTED_RESULT = "expected result";

        private final PrioritizedRequestWithResult<String, Object> thisRequest =
                new PrioritizedRequestWithResult<>(KEY, LOWEST);

        private final PrioritizedRequest<String, Object> otherRequest = new PrioritizedRequest<>(KEY, STANDARD);

        private final PrioritizedRequestWithResult<String, Object> merged =
                thisRequest.createMergedReplacement(otherRequest);

        @Test
        void has_same_key_and_higher_priority() {
            assertThat(merged.getKey()).isEqualTo(KEY);
            assertThat(merged.getPriority()).isEqualTo(STANDARD);
        }

        @Test
        void forwards_result_to_existing_futures() {
            merged.provideResult(EXPECTED_RESULT);

            assertThat(thisRequest.getResult()).contains(EXPECTED_RESULT);
        }

        @Test
        void forwards_cancellation_to_existing_futures() {
            merged.stopWithoutResult();

            assertThat(thisRequest.getResult()).isEmpty();
        }
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(PrioritizedRequestWithResult.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        assertThat(new PrioritizedRequestWithResult<>("key", STANDARD)).hasToString(
                "PrioritizedRequestWithResult{" +
                "key=key" +
                ", priority=STANDARD" +
                "}");
    }
}
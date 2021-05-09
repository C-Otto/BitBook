package de.cotto.bitbook.backend.request;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;

class PrioritizedRequestTest {

    public static final String KEY = "key";
    public static final RequestPriority REQUEST_PRIORITY = STANDARD;

    @Test
    void getKey() {
        assertThat(new TestablePrioritizedRequest(REQUEST_PRIORITY, KEY).getKey()).isEqualTo("key");
    }

    @Test
    void getPriority() {
        assertThat(new TestablePrioritizedRequest(REQUEST_PRIORITY, KEY).getPriority()).isEqualTo(REQUEST_PRIORITY);
    }

    @Test
    void getWithResultFuture() {
        PrioritizedRequestWithResult<String, Object> prioritizedRequestWithResult =
                new TestablePrioritizedRequest(REQUEST_PRIORITY, KEY).getWithResultFuture();
        assertThat(prioritizedRequestWithResult.getKey()).isEqualTo(KEY);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(PrioritizedRequest.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        assertThat(new TestablePrioritizedRequest(REQUEST_PRIORITY, KEY)).hasToString(
                "PrioritizedRequest{" +
                "key=key" +
                ", priority=STANDARD" +
                "}"
        );
    }

    private static class TestablePrioritizedRequest extends PrioritizedRequest<String, Object> {
        public TestablePrioritizedRequest(RequestPriority priority, String key) {
            super(key, priority);
        }
    }
}
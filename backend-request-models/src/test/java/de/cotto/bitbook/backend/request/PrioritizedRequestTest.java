package de.cotto.bitbook.backend.request;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;

class PrioritizedRequestTest {

    public static final String KEY = "key";
    public static final RequestPriority REQUEST_PRIORITY = STANDARD;

    @Nullable
    private Object seen;

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
    void withResultConsumer() {
        TestablePrioritizedRequest request =
                new TestablePrioritizedRequest(REQUEST_PRIORITY, KEY, this::resultConsumer);
        request.getWithResultFuture().provideResult("result");
        assertThat(seen).isEqualTo("result");
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(PrioritizedRequest.class).usingGetClass().verify();
    }

    @Test
    void testToString_with_result_consumer() {
        assertThat(new TestablePrioritizedRequest(REQUEST_PRIORITY, KEY, this::resultConsumer)).hasToString(
                "PrioritizedRequest{" +
                "key=key" +
                ", priority=STANDARD" +
                "}"
        );
    }

    @Test
    void testToString_without_result_consumer() {
        assertThat(new TestablePrioritizedRequest(REQUEST_PRIORITY, KEY)).hasToString(
                "PrioritizedRequest{" +
                "key=key" +
                ", priority=STANDARD" +
                "}"
        );
    }

    private void resultConsumer(Object result) {
        this.seen = result;
    }

    private static class TestablePrioritizedRequest extends PrioritizedRequest<String, Object> {
        public TestablePrioritizedRequest(RequestPriority priority, String key, Consumer<Object> resultConsumer) {
            super(key, priority, resultConsumer);
        }

        public TestablePrioritizedRequest(RequestPriority priority, String key) {
            super(key, priority);
        }
    }
}
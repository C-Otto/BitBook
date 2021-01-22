package de.cotto.bitbook.backend.request;

import java.util.Objects;
import java.util.function.Consumer;

public class PrioritizedRequest<K, R> {
    private final K key;
    private final RequestPriority priority;
    private final Consumer<R> resultConsumer;

    protected PrioritizedRequest(K key, RequestPriority priority) {
        this(key, priority, result -> {});
    }

    protected PrioritizedRequest(K key, RequestPriority priority, Consumer<R> resultConsumer) {
        super();
        this.key = key;
        this.priority = priority;
        this.resultConsumer = resultConsumer;
    }

    public RequestPriority getPriority() {
        return priority;
    }

    public K getKey() {
        return key;
    }

    public PrioritizedRequestWithResult<K,R> getWithResultFuture() {
        return new PrioritizedRequestWithResult<>(key, priority, resultConsumer);
    }

    @Override
    public String toString() {
        return "PrioritizedRequest{" +
               "key=" + key +
               ", priority=" + priority +
               '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        PrioritizedRequest<?, ?> that = (PrioritizedRequest<?, ?>) other;
        return Objects.equals(key, that.key)
               && priority == that.priority
               && Objects.equals(resultConsumer, that.resultConsumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, priority, resultConsumer);
    }
}

package de.cotto.bitbook.backend.request;

import java.util.Objects;

public class PrioritizedRequest<K, R> {
    private final K key;
    private final RequestPriority priority;

    protected PrioritizedRequest(K key, RequestPriority priority) {
        super();
        this.key = key;
        this.priority = priority;
    }

    public RequestPriority getPriority() {
        return priority;
    }

    public K getKey() {
        return key;
    }

    public PrioritizedRequestWithResult<K,R> getWithResultFuture() {
        return new PrioritizedRequestWithResult<>(key, priority);
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
               && priority == that.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, priority);
    }
}

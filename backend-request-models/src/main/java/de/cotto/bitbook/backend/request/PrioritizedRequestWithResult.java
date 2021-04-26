package de.cotto.bitbook.backend.request;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Objects;

public class PrioritizedRequestWithResult<K, R> extends ResultFuture<R>
        implements Comparable<PrioritizedRequestWithResult<K, R>> {

    private final K key;
    private final RequestPriority priority;

    public PrioritizedRequestWithResult(K key, RequestPriority priority) {
        super();
        this.key = key;
        this.priority = priority;
    }

    public K getKey() {
        return key;
    }

    public RequestPriority getPriority() {
        return priority;
    }

    public PrioritizedRequestWithResult<K, R> createMergedReplacement(PrioritizedRequest<K, R> newRequest) {
        PrioritizedRequestWithResult<K, R> replacementRequest =
                new PrioritizedRequestWithResult<>(key, priority.getHighestPriority(newRequest.getPriority()));
        replacementRequest.addResultListener(this);
        return replacementRequest;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        PrioritizedRequestWithResult<?, ?> that = (PrioritizedRequestWithResult<?, ?>) other;
        return Objects.equals(key, that.key) && priority == that.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), key, priority);
    }

    @Override
    public int compareTo(@Nonnull PrioritizedRequestWithResult<K, R> other) {
        int byPriority = Comparator.comparing(RequestPriority::getIntegerForComparison)
                .compare(priority, other.priority);
        if (byPriority != 0) {
            return byPriority;
        }
        if (this.equals(other)) {
            return 0;
        }
        int byKey = Comparator.comparing(System::identityHashCode).compare(key, other.key);
        if (byKey != 0) {
            return byKey;
        }
        return Comparator.comparing(System::identityHashCode).compare(this, other);
    }

    @Override
    public String toString() {
        return "PrioritizedRequestWithResult{" +
               "key=" + key +
               ", priority=" + priority +
               "}";
    }
}

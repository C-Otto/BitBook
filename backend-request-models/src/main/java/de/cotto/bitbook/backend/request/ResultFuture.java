package de.cotto.bitbook.backend.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ResultFuture<R> {
    private final CompletableFuture<R> completableFuture;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ResultFuture() {
        this.completableFuture = new CompletableFuture<>();
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    public void addResultListener(ResultFuture<R> listener) {
        completableFuture.whenComplete((result, exception) -> {
            if (exception == null) {
                listener.provideResult(result);
            } else {
                listener.stopWithoutResult();
            }
        });
    }

    public void provideResult(@Nonnull R result) {
        completableFuture.complete(result);
    }

    public void stopWithoutResult() {
        completableFuture.cancel(false);
    }

    public Optional<R> getResult() {
        try {
            return Optional.of(completableFuture.get());
        } catch (InterruptedException | ExecutionException | CancellationException e) {
            logger.debug("Exception while waiting for result: {}", this, e);
            return Optional.empty();
        }
    }

    public static <R> R getOrElse(Future<R> future, R other) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException | CancellationException e) {
            return other;
        }
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    public CompletableFuture<R> getFuture() {
        CompletableFuture<R> future = new CompletableFuture<>();
        completableFuture.whenComplete((result, exception) -> {
            if (result == null) {
                future.completeExceptionally(exception);
            } else {
                future.complete(result);
            }
        });
        return future;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ResultFuture<?> that = (ResultFuture<?>) other;
        return Objects.equals(completableFuture, that.completableFuture)
               && Objects.equals(logger, that.logger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(completableFuture, logger);
    }
}

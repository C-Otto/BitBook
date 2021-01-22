package de.cotto.bitbook.backend.request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public final class ResultFromProvider<R> {
    @Nullable
    private final R result;
    private final boolean successful;

    private ResultFromProvider(@Nonnull R result) {
        this.result = result;
        successful = true;
    }

    @SuppressWarnings("PMD.NullAssignment")
    private ResultFromProvider(boolean successful) {
        this.result = null;
        this.successful = successful;
    }

    public static <R> ResultFromProvider<R> of(R result) {
        return new ResultFromProvider<>(result);
    }

    public static <R> ResultFromProvider<R> empty() {
        return new ResultFromProvider<>(true);
    }

    public static <R> ResultFromProvider<R> failure() {
        return new ResultFromProvider<>(false);
    }

    public Optional<R> getAsOptional() {
        return Optional.ofNullable(result);
    }

    public boolean isSuccessful() {
        return successful;
    }
}

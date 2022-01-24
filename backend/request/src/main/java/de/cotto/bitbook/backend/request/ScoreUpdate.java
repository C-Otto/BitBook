package de.cotto.bitbook.backend.request;

import org.springframework.http.HttpStatus;

public class ScoreUpdate {
    public static final ScoreUpdate RATE_LIMITED = new ScoreUpdate(1_500);
    public static final ScoreUpdate CIRCUIT_BREAKER = new ScoreUpdate(2_000);
    public static final ScoreUpdate PROVIDER_EXCEPTION = new ScoreUpdate(4_500);
    public static final ScoreUpdate UNKNOWN_EXCEPTION = new ScoreUpdate(5_000);

    private final long value;

    protected ScoreUpdate(long value) {
        this.value = value;
    }

    public static ScoreUpdate forHttpStatus(int status) {
        if (status == HttpStatus.TOO_MANY_REQUESTS.value()) {
            return new ScoreUpdate(4_000);
        }
        return new ScoreUpdate(3_000);
    }

    public static ScoreUpdate forSuccess(long durationInMilliSeconds) {
        return new ScoreUpdate(durationInMilliSeconds);
    }

    public long getValue() {
        return value;
    }
}

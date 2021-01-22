package de.cotto.bitbook.backend.request;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreUpdateTest {

    @Test
    void circuitBreakerException_smallerThan_httpFailure() {
        assertThat(ScoreUpdate.CIRCUIT_BREAKER.getValue()).isLessThan(ScoreUpdate.forHttpStatus(0).getValue());
    }

    @Test
    void rateLimited_smallerThan_circuitBreaker() {
        assertThat(ScoreUpdate.RATE_LIMITED.getValue()).isLessThan(ScoreUpdate.CIRCUIT_BREAKER.getValue());
    }

    @Test
    void rateLimited_smallerThan_httpFailure() {
        assertThat(ScoreUpdate.RATE_LIMITED.getValue()).isLessThan(ScoreUpdate.forHttpStatus(0).getValue());
    }

    @Test
    void httpFailure_smallerThan_tooManyRequests() {
        assertThat(ScoreUpdate.forHttpStatus(0).getValue())
                .isLessThan(ScoreUpdate.forHttpStatus(HttpStatus.TOO_MANY_REQUESTS.value()).getValue());
    }

    @Test
    void fast_success_smallerThan_slow_success() {
        assertThat(ScoreUpdate.forSuccess(100).getValue()).isLessThan(ScoreUpdate.forSuccess(1_000).getValue());
    }

    @Test
    void slow_success_smallerThan_rate_limited() {
        assertThat(ScoreUpdate.forSuccess(1_000).getValue()).isLessThan(ScoreUpdate.RATE_LIMITED.getValue());
    }
}
package de.cotto.bitbook.backend.request;

import de.cotto.bitbook.backend.ProviderException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.org.lidalia.slf4jtest.LoggingEvent.debug;
import static uk.org.lidalia.slf4jtest.LoggingEvent.error;
import static uk.org.lidalia.slf4jtest.LoggingEvent.warn;

@ExtendWith(MockitoExtension.class)
class RequestWorkerTest {
    private final TestLogger logger = TestLoggerFactory.getTestLogger(RequestWorker.class);
    private static final String KEY = "a";

    private RequestWorker<String, Integer> requestWorker;

    private TestableProvider provider1;
    private TestableProvider provider2;

    @BeforeEach
    void setUp() {
        logger.clearAll();
        provider1 = spy(new TestableProvider());
        provider2 = spy(new TestableProvider());
        requestWorker = new RequestWorker<>(List.of(provider1, provider2));
    }

    @Test
    void getNow() throws AllProvidersFailedException {
        Optional<Integer> result = requestWorker.getNow("x");
        assertThat(result).contains(1);
        verifyNoInteractions(provider2);
    }

    @Test
    void getNow_no_result() throws AllProvidersFailedException {
        Optional<Integer> result = requestWorker.getNow("");
        assertThat(result).isEmpty();
        verifyNoInteractions(provider2);
    }

    @Test
    void uses_second_provider_on_request_not_permitted_exception() throws Exception {
        when(provider1.get(any())).thenThrow(mock(RequestNotPermitted.class));
        when(provider2.get(any())).thenCallRealMethod();
        Optional<Integer> result = requestWorker.getNow("xxx");
        assertThat(result).contains(3);
    }

    @Test
    void uses_second_provider_on_call_not_permitted_exception() throws Exception {
        when(provider1.get(any())).thenThrow(mock(CallNotPermittedException.class));
        when(provider2.get(any())).thenCallRealMethod();
        Optional<Integer> result = requestWorker.getNow("abc");
        assertThat(result).contains(3);
    }

    @Test
    void uses_second_provider_on_feign_exception() throws Exception {
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenCallRealMethod();
        Optional<Integer> result = requestWorker.getNow("def");
        assertThat(result).contains(3);
    }

    @Test
    void all_providers_fail() throws Exception {
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenThrow(mock(CallNotPermittedException.class));
        assertThatExceptionOfType(AllProvidersFailedException.class).isThrownBy(() ->
            requestWorker.getNow("xyz")
        );
    }

    @Test
    void reuses_first_provider_after_successful_request() throws Exception {
        request(2);

        verify(provider1).get(KEY);
        verifyNoInteractions(provider2);
    }

    @Test
    void prefers_second_provider_after_failure_of_first_provider() throws Exception {
        when(provider1.get(any())).thenThrow(mock(RequestNotPermitted.class));
        request(2);

        verifyNoInteractions(provider1);
        verify(provider2).get(KEY);
    }

    @Test
    void unknown_exceptions_are_worse_than_feign_exceptions() throws Exception {
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenThrow(mock(ArithmeticException.class));
        request(2);

        verify(provider1).get(KEY);
        verifyNoInteractions(provider2);
    }

    @Test
    void feign_exceptions_are_worse_than_circuit_breaker_exceptions() throws Exception {
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenThrow(mock(CallNotPermittedException.class));
        request(2);

        verifyNoInteractions(provider1);
        verify(provider2).get(KEY);
    }

    @Test
    void feign_exceptions_are_worse_than_rate_limiter_exceptions() throws Exception {
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenThrow(mock(RequestNotPermitted.class));
        request(2);

        verifyNoInteractions(provider1);
        verify(provider2).get(KEY);
    }

    @Test
    void circuit_breaker_exceptions_are_worse_than_rate_limiter_exceptions() throws Exception {
        when(provider1.get(any())).thenThrow(mock(RequestNotPermitted.class));
        when(provider2.get(any())).thenThrow(mock(CallNotPermittedException.class));
        request(2);

        verify(provider1).get(KEY);
        verifyNoInteractions(provider2);
    }

    @Test
    void too_many_requests_is_worse_than_default_feign_exception() throws Exception {
        FeignException feignException = tooManyRequests();
        when(provider1.get(any())).thenThrow(feignException);
        when(provider2.get(any())).thenThrow(mock(FeignException.class));
        request(2);

        verifyNoInteractions(provider1);
        verify(provider2).get(KEY);
    }

    @Test
    void provider_exception_is_worse_than_too_many_requests() throws Exception {
        FeignException tooManyRequests = tooManyRequests();
        when(provider1.get(any())).thenThrow(tooManyRequests);
        when(provider2.get(any())).thenThrow(ProviderException.class);
        request(2);

        verify(provider1).get(KEY);
        verifyNoInteractions(provider2);
    }

    @Test
    void unknown_exception_is_worse_than_provider_exception() throws Exception {
        when(provider1.get(any())).thenThrow(ProviderException.class);
        when(provider2.get(any())).thenThrow(mock(ArithmeticException.class));
        request(2);

        verify(provider1).get(KEY);
        verifyNoInteractions(provider2);
    }

    @Test
    void logs_warning_with_debug_reason_for_request_not_permitted_exception() throws Exception {
        Exception exception1 = mock(RequestNotPermitted.class);
        Exception exception2 = mock(RequestNotPermitted.class);
        assertWarnAndDebugLogs(exception1, exception2, "{} is rate limited, skipping.");
    }

    @Test
    void logs_warning_with_debug_reason_for_call_not_permitted_exception() throws Exception {
        Exception exception1 = mock(CallNotPermittedException.class);
        Exception exception2 = mock(CallNotPermittedException.class);
        assertWarnAndDebugLogs(exception1, exception2, "{} is disabled via circuit breaker, skipping.");
    }

    @Test
    void logs_warning_with_debug_reason_for_feign_exception() throws Exception {
        Exception exception1 = mock(FeignException.class);
        Exception exception2 = tooManyRequests();
        assertWarnAndDebugLogs(exception1, exception2, "{} experienced feign issue, skipping.");
    }

    @Test
    void logs_warning_with_debug_reason_for_provider_exception() throws Exception {
        Exception exception1 = new ProviderException(new ArithmeticException());
        Exception exception2 = new ProviderException(new IllegalStateException());
        assertWarnAndDebugLogs(exception1, exception2, "{} threw provider exception, skipping.");
    }

    @Test
    void logs_details_for_unknown_exception_on_error_level() throws Exception {
        Exception exception1 = new ArithmeticException("foo");
        Exception exception2 = new ArithmeticException("bar");
        when(provider1.get(any())).thenThrow(exception1);
        when(provider2.get(any())).thenThrow(exception2);
        request(2);
        assertThat(logger.getLoggingEvents()).containsExactlyInAnyOrder(
                error(exception1, "{} threw unknown exception: ", provider1.getName()),
                error(exception2, "{} threw unknown exception: ", provider2.getName())
        );
    }

    @Test
    void one_failure_is_better_than_two_failures() throws Exception {
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenThrow(mock(FeignException.class)).thenCallRealMethod();
        request(3);

        verifyNoInteractions(provider1);
        verify(provider2).get(KEY);
    }

    @Test
    void fast_success_is_better_than_slow_success() throws Exception {
        when(provider1.get(any())).then(invocation -> {
            Thread.sleep(200);
            return Optional.of(1);
        });
        when(provider2.get(any())).then(invocation -> {
            Thread.sleep(100);
            return Optional.of(1);
        });
        request(3);

        verifyNoInteractions(provider1);
        verify(provider2).get(KEY);
    }

    @Test
    void only_consider_last_five_requests() throws Exception {
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenCallRealMethod().thenThrow(mock(FeignException.class));
        request(7);

        verify(provider1).get(KEY);
        verifyNoInteractions(provider2);
    }

    private void assertWarnAndDebugLogs(
            Exception exception1,
            Exception exception2,
            String expectedMessage)
            throws Exception {
        when(provider1.get(any())).thenThrow(exception1);
        when(provider2.get(any())).thenThrow(exception2);
        request(2);
        assertThat(logger.getLoggingEvents()).containsExactlyInAnyOrder(
                warn(expectedMessage, provider1.getName()),
                warn(expectedMessage, provider2.getName()),
                debug(exception1, "Reason: "),
                debug(exception2, "Reason: ")
        );
    }

    private FeignException tooManyRequests() {
        FeignException tooManyRequests = mock(FeignException.class);
        when(tooManyRequests.status()).thenReturn(HttpStatus.TOO_MANY_REQUESTS.value());
        return tooManyRequests;
    }

    private void request(int numberOfInvocations) throws Exception {
        for (int i = 0; i < numberOfInvocations - 1; i++) {
            get();
        }
        Mockito.reset(provider1, provider2);
        lenient().when(provider1.get(KEY)).thenCallRealMethod();
        lenient().when(provider2.get(KEY)).thenCallRealMethod();
        get();
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    private void get() {
        try {
            requestWorker.getNow(KEY);
        } catch (AllProvidersFailedException exception) {
            // ignored
        }
    }
}
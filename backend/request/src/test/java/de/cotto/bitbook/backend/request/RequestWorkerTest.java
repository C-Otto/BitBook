package de.cotto.bitbook.backend.request;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

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

@ExtendWith(MockitoExtension.class)
class RequestWorkerTest {
    private static final String KEY = "a";

    private RequestWorker<String, Integer> requestWorker;

    private TestableProvider provider1;
    private TestableProvider provider2;

    @BeforeEach
    void setUp() {
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
    void uses_second_provider_on_request_not_permitted_exception() throws AllProvidersFailedException {
        when(provider1.get(any())).thenThrow(mock(RequestNotPermitted.class));
        when(provider2.get(any())).thenCallRealMethod();
        Optional<Integer> result = requestWorker.getNow("xxx");
        assertThat(result).contains(3);
    }

    @Test
    void uses_second_provider_on_call_not_permitted_exception() throws AllProvidersFailedException {
        when(provider1.get(any())).thenThrow(mock(CallNotPermittedException.class));
        when(provider2.get(any())).thenCallRealMethod();
        Optional<Integer> result = requestWorker.getNow("abc");
        assertThat(result).contains(3);
    }

    @Test
    void uses_second_provider_on_feign_exception() throws AllProvidersFailedException {
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenCallRealMethod();
        Optional<Integer> result = requestWorker.getNow("def");
        assertThat(result).contains(3);
    }

    @Test
    void all_providers_fail() {
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenThrow(mock(CallNotPermittedException.class));
        assertThatExceptionOfType(AllProvidersFailedException.class).isThrownBy(() ->
            requestWorker.getNow("xyz")
        );
    }

    @Test
    void reuses_first_provider_after_successful_request() {
        request(2);

        verify(provider1).get(KEY);
        verifyNoInteractions(provider2);
    }

    @Test
    void prefers_second_provider_after_failure_of_first_provider() {
        when(provider1.get(any())).thenThrow(mock(RequestNotPermitted.class));
        request(2);

        verifyNoInteractions(provider1);
        verify(provider2).get(KEY);
    }

    @Test
    void unknown_exceptions_are_worse_than_feign_exceptions() {
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenThrow(mock(ArithmeticException.class));
        request(2);

        verify(provider1).get(KEY);
        verifyNoInteractions(provider2);
    }

    @Test
    void feign_exceptions_are_worse_than_circuit_breaker_exceptions() {
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenThrow(mock(CallNotPermittedException.class));
        request(2);

        verifyNoInteractions(provider1);
        verify(provider2).get(KEY);
    }

    @Test
    void feign_exceptions_are_worse_than_rate_limiter_exceptions() {
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenThrow(mock(RequestNotPermitted.class));
        request(2);

        verifyNoInteractions(provider1);
        verify(provider2).get(KEY);
    }

    @Test
    void circuit_breaker_exceptions_are_worse_than_rate_limiter_exceptions() {
        when(provider1.get(any())).thenThrow(mock(RequestNotPermitted.class));
        when(provider2.get(any())).thenThrow(mock(CallNotPermittedException.class));
        request(2);

        verify(provider1).get(KEY);
        verifyNoInteractions(provider2);
    }

    @Test
    void too_many_requests_is_worse_than_default_feign_exception() {
        FeignException feignException = tooManyRequests();
        when(provider1.get(any())).thenThrow(feignException);
        when(provider2.get(any())).thenThrow(mock(FeignException.class));
        request(2);

        verifyNoInteractions(provider1);
        verify(provider2).get(KEY);
    }

    @Test
    void unknown_exception_is_worse_than_too_many_requests() {
        FeignException tooManyRequests = tooManyRequests();
        when(provider1.get(any())).thenThrow(tooManyRequests);
        when(provider2.get(any())).thenThrow(mock(ArithmeticException.class));
        request(2);

        verify(provider1).get(KEY);
        verifyNoInteractions(provider2);
    }

    @Test
    void one_failure_is_better_than_two_failures() {
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenThrow(mock(FeignException.class)).thenCallRealMethod();
        request(3);

        verifyNoInteractions(provider1);
        verify(provider2).get(KEY);
    }

    @Test
    void fast_success_is_better_than_slow_success() {
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
    void only_consider_last_five_requests() {
        when(provider1.get(any())).thenThrow(mock(FeignException.class));
        when(provider2.get(any())).thenCallRealMethod().thenThrow(mock(FeignException.class));
        request(7);

        verify(provider1).get(KEY);
        verifyNoInteractions(provider2);
    }

    private FeignException tooManyRequests() {
        FeignException tooManyRequests = mock(FeignException.class);
        when(tooManyRequests.status()).thenReturn(HttpStatus.TOO_MANY_REQUESTS.value());
        return tooManyRequests;
    }

    private void request(int numberOfInvocations) {
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
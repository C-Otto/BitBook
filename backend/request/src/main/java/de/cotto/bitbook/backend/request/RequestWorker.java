package de.cotto.bitbook.backend.request;

import com.google.common.annotations.VisibleForTesting;
import de.cotto.bitbook.backend.Provider;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByValue;
import static java.util.Objects.requireNonNull;

@Component
public class RequestWorker<K, R> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<Provider<K, R>, Score> providerScores;

    public RequestWorker(List<? extends Provider<K, R>> providers) {
        providerScores = Collections.synchronizedMap(new LinkedHashMap<>());
        providers.forEach(provider -> providerScores.put(provider, Score.DEFAULT));
    }

    public Optional<R> getNow(K key) throws AllProvidersFailedException {
        for (Provider<K, R> provider : getSortedProviders()) {
            ResultFromProvider<R> resultFromProvider = getWithProvider(key, provider);
            if (resultFromProvider.isSuccessful()) {
                return resultFromProvider.getAsOptional();
            }
        }
        throw new AllProvidersFailedException();
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private ResultFromProvider<R> getWithProvider(K key, Provider<K, R> provider) {
        String providerName = provider.getName();
        try {
            long start = System.nanoTime();
            ResultFromProvider<R> result = provider.get(key)
                    .map(ResultFromProvider::of)
                    .orElse(ResultFromProvider.empty());
            long end = System.nanoTime();
            updateScore(provider, ScoreUpdate.forSuccess((end - start) / 1_000_000));
            return result;
        } catch (RequestNotPermitted exception) {
            logger.warn("{} is rate limited, skipping.", providerName);
            logger.debug("Reason: ", exception);
            updateScore(provider, ScoreUpdate.RATE_LIMITED);
        } catch (CallNotPermittedException exception) {
            logger.warn("{} is disabled via circuit breaker, skipping.", providerName);
            logger.debug("Reason: ", exception);
            updateScore(provider, ScoreUpdate.CIRCUIT_BREAKER);
        } catch (FeignException exception) {
            logger.warn("{} experienced feign issue, skipping.", providerName);
            logger.debug("Reason: ", exception);
            updateScore(provider, ScoreUpdate.forHttpStatus(exception.status()));
        } catch (Exception exception) {
            logger.error("{} threw unknown exception: ", providerName, exception);
            updateScore(provider, ScoreUpdate.UNKNOWN_EXCEPTION);
        }
        return ResultFromProvider.failure();
    }

    private void updateScore(Provider<K, R> provider, ScoreUpdate update) {
        providerScores.compute(provider, (key, currentValue) -> requireNonNull(currentValue).add(update));
    }

    private Iterable<? extends Provider<K, R>> getSortedProviders() {
        synchronized (providerScores) {
            return providerScores.entrySet().stream()
                    .sorted(comparingByValue())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }
    }

    @VisibleForTesting
    public void resetScores() {
        synchronized (providerScores) {
            for (Map.Entry<Provider<K, R>, Score> entry : providerScores.entrySet()) {
                entry.setValue(Score.DEFAULT);
            }
        }
    }
}

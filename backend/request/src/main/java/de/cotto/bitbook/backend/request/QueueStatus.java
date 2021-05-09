package de.cotto.bitbook.backend.request;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class QueueStatus {
    private final List<PrioritizingProvider<?, ?>> providers;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<PrioritizingProvider<?, ?>, Map<RequestPriority, Integer>> previous = new LinkedHashMap<>();

    public QueueStatus(List<PrioritizingProvider<?, ?>> providers) {
        this.providers = providers;
    }

    @Scheduled(fixedRate = 5_000)
    public void logQueueStatus() {
        for (PrioritizingProvider<?, ?> provider : providers) {
            logQueueStatus(provider);
        }
    }

    private <K, R> void logQueueStatus(PrioritizingProvider<K, R> provider) {
        for (Map.Entry<RequestPriority, List<PrioritizedRequestWithResult<K, R>>> entry :
                provider.getQueueByPriority().entrySet()) {
            logForRequestsWithPriority(provider, entry);
        }
    }

    private void logForRequestsWithPriority(
            PrioritizingProvider<?, ?> provider, Map.Entry<RequestPriority, ? extends List<?>> entry
    ) {
        previous.putIfAbsent(provider, new LinkedHashMap<>());
        RequestPriority requestPriority = entry.getKey();
        if (requestPriority != RequestPriority.LOWEST) {
            int requestsInQueue = entry.getValue().size();
            String formattedDifference = getFormattedDifference(provider, requestPriority, requestsInQueue);
            log(provider, requestPriority, requestsInQueue, formattedDifference);
            previous.getOrDefault(provider, Map.of()).put(requestPriority, requestsInQueue);
        }
    }

    private String getFormattedDifference(
            PrioritizingProvider<?, ?> provider,
            RequestPriority requestPriority,
            int requestsInQueue
    ) {
        Integer previousCount = previous.getOrDefault(provider, Map.of()).get(requestPriority);
        if (previousCount == null) {
            return "";
        }
        int difference = requestsInQueue - previousCount;
        if (difference >= 0) {
            return " (+" + difference + ")";
        }
        return " (" + difference + ")";
    }

    @VisibleForTesting
    protected void log(
            PrioritizingProvider<?, ?> provider,
            RequestPriority requestPriority,
            int outstanding,
            String formattedDifference
    ) {
        logger.info("{}{} outstanding requests for '{}' (priority {})",
                outstanding,
                formattedDifference,
                provider.getProvidedResultName(),
                requestPriority
        );
    }
}

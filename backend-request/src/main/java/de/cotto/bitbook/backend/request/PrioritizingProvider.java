package de.cotto.bitbook.backend.request;

import com.google.common.annotations.VisibleForTesting;
import de.cotto.bitbook.backend.Provider;
import org.apache.commons.collections4.QueueUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

public class PrioritizingProvider<K, R> {
    protected final Queue<PrioritizedRequestWithResult<K, R>> requestQueue;
    protected final List<PrioritizedRequestWithResult<K, R>> runningRequests;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AtomicBoolean workOnRequestsIsDisabled;
    private final String providedResultName;
    private final RequestWorker<K, R> requestWorker;

    protected PrioritizingProvider(List<? extends Provider<K, R>> providers, String providedResultName) {
        this(new AtomicBoolean(false), providers, providedResultName);
    }

    @VisibleForTesting
    PrioritizingProvider(
            AtomicBoolean workOnRequestsIsDisabled,
            List<? extends Provider<K, R>> providers,
            String providedResultName
    ) {
        this.workOnRequestsIsDisabled = workOnRequestsIsDisabled;
        requestQueue = QueueUtils.synchronizedQueue(new PriorityQueue<>());
        runningRequests = Collections.synchronizedList(new ArrayList<>());
        this.providedResultName = providedResultName;
        this.requestWorker = new RequestWorker<>(providers);
    }

    public String getProvidedResultName() {
        return providedResultName;
    }

    protected Optional<R> getForRequest(PrioritizedRequest<K, R> request) {
        logger.debug("Queuing {}", request);
        if (request.getPriority() == RequestPriority.LOWEST) {
            enqueue(request);
            return Optional.empty();
        }
        return enqueue(request).getResult();
    }

    @Scheduled(fixedDelay = 100)
    public void workOnRequests() {
        if (workOnRequestsIsDisabled.get()) {
            return;
        }
        PrioritizedRequestWithResult<K, R> prioritizedRequestWithResult;
        if (logger.isDebugEnabled() && !requestQueue.isEmpty()) {
            logger.debug("Queue size: {}", requestQueue.size());
        }
        do {
            prioritizedRequestWithResult = requestQueue.poll();
            if (prioritizedRequestWithResult != null) {
                workOnRequest(prioritizedRequestWithResult);
            }
        } while (prioritizedRequestWithResult != null);
    }

    private void workOnRequest(PrioritizedRequestWithResult<K, R> request) {
        runningRequests.add(request);
        logger.debug("Working on {}", request);
        try {
            requestWorker.getNow(request.getKey()).ifPresentOrElse(
                    request::provideResult,
                    request::stopWithoutResult
            );
        } catch (AllProvidersFailedException e) {
            logger.warn("All providers failed, removing lowest priority requests from queue");
            requestQueue.removeIf(queuedRequest -> queuedRequest.getPriority() == RequestPriority.LOWEST);
            request.stopWithoutResult();
        }
        runningRequests.remove(request);
    }

    private PrioritizedRequestWithResult<K, R> enqueue(PrioritizedRequest<K, R> request) {
        PrioritizedRequestWithResult<K, R> result = getResultFromRunningRequest(request).orElse(null);
        if (result != null) {
            return result;
        }

        synchronized (requestQueue) {
            Optional<PrioritizedRequestWithResult<K, R>> replacedRequest = replaceExistingRequest(request);
            if (replacedRequest.isPresent()) {
                return replacedRequest.get();
            }
            PrioritizedRequestWithResult<K, R> prioritizedRequestWithResult = request.getWithResultFuture();
            requestQueue.add(prioritizedRequestWithResult);
            return prioritizedRequestWithResult;
        }
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    private Optional<PrioritizedRequestWithResult<K, R>> getResultFromRunningRequest(PrioritizedRequest<K, R> request) {
        PrioritizedRequestWithResult<K, R> matchingRequest;
        synchronized (runningRequests) {
            matchingRequest = runningRequests.stream()
                    .filter(runningRequest -> request.getKey().equals(runningRequest.getKey()))
                    .findFirst()
                    .orElse(null);
        }
        if (matchingRequest == null) {
            return Optional.empty();
        }
        PrioritizedRequestWithResult<K, R> requestWithResultFuture = request.getWithResultFuture();
        matchingRequest.addResultListener(requestWithResultFuture);
        return Optional.of(requestWithResultFuture);
    }

    private Optional<PrioritizedRequestWithResult<K, R>> replaceExistingRequest(PrioritizedRequest<K, R> request) {
        return requestQueue.stream()
                .filter(queuedRequest -> queuedRequest.getKey().equals(request.getKey()))
                .findFirst()
                .map(replacedRequest -> {
                    logger.debug("Merged {} and existing request {}", request, replacedRequest);
                    requestQueue.remove(replacedRequest);
                    PrioritizedRequestWithResult<K, R> replacingRequest =
                            replacedRequest.createMergedReplacement(request);
                    requestQueue.add(replacingRequest);
                    return replacingRequest;
                });
    }

    @VisibleForTesting
    public Queue<PrioritizedRequestWithResult<K, R>> getRequestQueue() {
        return requestQueue;
    }

    protected Map<RequestPriority, List<PrioritizedRequestWithResult<K, R>>> getQueueByPriority() {
        synchronized (requestQueue) {
            Stream<PrioritizedRequestWithResult<K, R>> stream = requestQueue.stream();
            return stream.collect(groupingBy(PrioritizedRequestWithResult::getPriority));
        }
    }
}

package de.cotto.bitbook.backend.transaction;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static de.cotto.bitbook.backend.transaction.PrioritizingBlockHeightProvider.INVALID;

@Component
public class BlockHeightService {
    protected final LoadingCache<Integer, Integer> blockHeightCache;
    private int lastKnown = INVALID;
    private final PrioritizingBlockHeightProvider prioritizingBlockHeightProvider;

    public BlockHeightService(PrioritizingBlockHeightProvider prioritizingBlockHeightProvider) {
        this.prioritizingBlockHeightProvider = prioritizingBlockHeightProvider;
        blockHeightCache = CacheBuilder.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build(CacheLoader.from(this::getFromProvider));
    }

    public int getBlockHeight() {
        try {
            int result = blockHeightCache.get(0);
            if (result == INVALID) {
                return prioritizingBlockHeightProvider.getBlockHeight();
            }
            return result;
        } catch (ExecutionException e) {
            return INVALID;
        }
    }

    private int getFromProvider() {
        int fromProvider = prioritizingBlockHeightProvider.getBlockHeight();
        int newResult = Math.max(lastKnown, fromProvider);
        lastKnown = newResult;
        return newResult;
    }
}

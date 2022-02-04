package de.cotto.bitbook.backend.transaction;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.cotto.bitbook.backend.model.Chain;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static de.cotto.bitbook.backend.transaction.PrioritizingBlockHeightProvider.INVALID;

@Component
public class BlockHeightService {
    protected final LoadingCache<Chain, Integer> blockHeightCache;
    private final Map<Chain, Integer> lastKnown = new LinkedHashMap<>();
    private final PrioritizingBlockHeightProvider prioritizingBlockHeightProvider;

    public BlockHeightService(PrioritizingBlockHeightProvider prioritizingBlockHeightProvider) {
        this.prioritizingBlockHeightProvider = prioritizingBlockHeightProvider;
        blockHeightCache = CacheBuilder.newBuilder()
                .maximumSize(Chain.values().length)
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build(CacheLoader.from(this::getFromProvider));
    }

    public int getBlockHeight(Chain chain) {
        try {
            int result = blockHeightCache.get(chain);
            if (result == INVALID) {
                return prioritizingBlockHeightProvider.getBlockHeight(chain);
            }
            return result;
        } catch (ExecutionException e) {
            return INVALID;
        }
    }

    private int getFromProvider(Chain chain) {
        int fromProvider = prioritizingBlockHeightProvider.getBlockHeight(chain);
        int newResult = Math.max(lastKnown.getOrDefault(chain, INVALID), fromProvider);
        lastKnown.put(chain, newResult);
        return newResult;
    }
}

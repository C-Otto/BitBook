package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.request.PrioritizingProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrioritizingBlockHeightProvider extends PrioritizingProvider<Object, Integer> {
    protected static final int INVALID = -1;

    public PrioritizingBlockHeightProvider(List<BlockHeightProvider> providers) {
        super(providers, "Block height");
    }

    public int getBlockHeight() {
        return getForRequestBlocking(BlockHeightRequest.STANDARD_PRIORITY).orElse(INVALID);
    }
}

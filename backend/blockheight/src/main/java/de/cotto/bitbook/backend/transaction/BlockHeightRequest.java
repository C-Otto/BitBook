package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.request.PrioritizedRequest;

import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;

public final class BlockHeightRequest extends PrioritizedRequest<Chain, Integer> {
    public BlockHeightRequest(Chain chain) {
        super(chain, STANDARD);
    }
}

package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.request.PrioritizedRequest;

import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;

public final class BlockHeightRequest extends PrioritizedRequest<Object, Integer> {
    public static final PrioritizedRequest<Object, Integer> STANDARD_PRIORITY = new BlockHeightRequest();

    private BlockHeightRequest() {
        super("", STANDARD);
    }
}

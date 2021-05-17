package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import org.springframework.stereotype.Component;

@Component
public class TransactionUpdateHeuristics {
    private static final int BLOCKS_CONSIDERED_RECENT = 48;

    private final BlockHeightService blockHeightService;

    public TransactionUpdateHeuristics(BlockHeightService blockHeightService) {
        this.blockHeightService = blockHeightService;
    }

    public boolean isRecentEnough(AddressTransactions addressTransactions) {
        int currentBlockHeight = blockHeightService.getBlockHeight();
        return addressTransactions.getLastCheckedAtBlockHeight() + BLOCKS_CONSIDERED_RECENT >= currentBlockHeight;
    }
}

package de.cotto.bitbook.backend.transaction.blockchair;

import de.cotto.bitbook.backend.model.Chain;

public final class BlockchairChainName {
    private BlockchairChainName() {
        // utility class
    }

    static String get(Chain chain) {
        //noinspection EnhancedSwitchMigration
        switch (chain) {
            case BTC:
                return "bitcoin";
            case BCH:
                return "bitcoin-cash";
            case BSV:
                return "bitcoin-sv";
            default:
                throw new IllegalArgumentException();
        }
    }
}

package de.cotto.bitbook.backend.transaction.blockchair;

import de.cotto.bitbook.backend.model.Chain;

public final class BlockchairChainName {
    private BlockchairChainName() {
        // utility class
    }

    static String get(Chain chain) {
        return switch (chain) {
            case BTC -> "bitcoin";
            case BCH -> "bitcoin-cash";
            case BSV -> "bitcoin-sv";
            default -> throw new IllegalArgumentException();
        };
    }
}

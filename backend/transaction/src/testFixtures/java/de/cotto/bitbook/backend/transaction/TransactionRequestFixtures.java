package de.cotto.bitbook.backend.transaction;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;

public class TransactionRequestFixtures {
    public static final TransactionRequest TRANSACTION_REQUEST =
            new TransactionRequest(TRANSACTION_HASH, BTC, STANDARD);
}

package de.cotto.bitbook.backend.transaction;

import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;

public class TransactionRequestFixtures {
    public static final TransactionRequest TRANSACTION_REQUEST =
            new TransactionRequest(TRANSACTION_HASH, STANDARD);
}

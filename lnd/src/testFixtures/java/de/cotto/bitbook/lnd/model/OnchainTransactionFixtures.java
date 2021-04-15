package de.cotto.bitbook.lnd.model;

import de.cotto.bitbook.backend.transaction.model.Coins;

import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_VALUE_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;

public class OnchainTransactionFixtures {
    public static final OnchainTransaction ONCHAIN_TRANSACTION = new OnchainTransaction(
            TRANSACTION_HASH,
            "label",
            Coins.ofSatoshis(500),
            Coins.ofSatoshis(100)
    );

    public static final OnchainTransaction FUNDING_TRANSACTION = new OnchainTransaction(
            TRANSACTION_HASH,
            "",
            OUTPUT_VALUE_2,
            Coins.NONE
    );
}

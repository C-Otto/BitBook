package de.cotto.bitbook.lnd.model;

import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Transaction;

import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_VALUE_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
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
    public static final Transaction FUNDING_TRANSACTION_DETAILS = TRANSACTION;

    public static final OnchainTransaction OPENING_TRANSACTION = new OnchainTransaction(
            TRANSACTION_HASH,
            "",
            Coins.ofSatoshis(-1_234 - 21_513),
            Coins.ofSatoshis(21_513)
    );
    public static final Transaction OPENING_TRANSACTION_DETAILS = TRANSACTION;
}

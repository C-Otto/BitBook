package de.cotto.bitbook.lnd.model;

import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Transaction;

import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS_2;
import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS_3;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_VALUE_2;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_VALUE_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_VALUE_2;
import static de.cotto.bitbook.backend.model.TransactionFixtures.FEES;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_3;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;

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

    public static final OnchainTransaction TRANSACTION_WITH_OWNED_ADDRESSES = new OnchainTransaction(
            TRANSACTION_HASH,
            "",
            Coins.NONE,
            Coins.NONE,
            Set.of(ADDRESS_2, ADDRESS_3)
    );

    public static final Transaction OPENING_TRANSACTION_DETAILS = TRANSACTION;
    public static final OnchainTransaction OPENING_TRANSACTION = new OnchainTransaction(
            TRANSACTION_HASH,
            "",
            Coins.NONE.subtract(OUTPUT_VALUE_2).subtract(OPENING_TRANSACTION_DETAILS.getFees()),
            OPENING_TRANSACTION_DETAILS.getFees()
    );

    public static final OnchainTransaction OPENING_TRANSACTION_WITH_LABEL = new OnchainTransaction(
            OPENING_TRANSACTION.transactionHash(),
            "0:openchannel:foo",
            OPENING_TRANSACTION.amount(),
            OPENING_TRANSACTION.fees()
    );

    public static final String POOL_ACCOUNT_ID = "001a2021f4013201230af5013021302130f501a302130412fa1230213041030123";

    public static final Transaction POOL_ACCOUNT_CREATION_DETAILS = TRANSACTION;
    private static final String ACCOUNT_CREATION_PREFIX = " poold -- AccountCreation(acct_key=";
    private static final String ACCOUNT_CREATION_SUFFIX = ")";
    public static final Transaction POOL_ACCOUNT_CLOSE_DETAILS = TRANSACTION_3;
    public static final OnchainTransaction POOL_ACCOUNT_CREATION = new OnchainTransaction(
            TRANSACTION_HASH,
            ACCOUNT_CREATION_PREFIX + POOL_ACCOUNT_ID + ACCOUNT_CREATION_SUFFIX,
            Coins.ofSatoshis(-1_234 - 999),
            Coins.ofSatoshis(999)
    );

    private static final String MODIFICATION_PREFIX = " poold -- AccountModification(acct_key=";
    private static final String CLOSE_SUFFIX = ", expiry=false, deposit=false, is_close=true)";
    public static final OnchainTransaction POOL_ACCOUNT_CLOSE = new OnchainTransaction(
            TRANSACTION_HASH,
            MODIFICATION_PREFIX + POOL_ACCOUNT_ID + CLOSE_SUFFIX,
            OUTPUT_VALUE_1,
            Coins.NONE
    );

    private static final String CLOSE_SUFFIX_EXPIRY = ", expiry=true, deposit=false, is_close=true)";
    public static final OnchainTransaction POOL_ACCOUNT_CLOSE_EXPIRY = new OnchainTransaction(
            TRANSACTION_HASH,
            MODIFICATION_PREFIX + POOL_ACCOUNT_ID + CLOSE_SUFFIX_EXPIRY,
            OUTPUT_VALUE_1,
            Coins.NONE
    );

    public static final Transaction POOL_ACCOUNT_DEPOSIT_DETAILS = TRANSACTION;
    private static final String DEPOSIT_SUFFIX = ", expiry=false, deposit=true, is_close=false)";
    public static final OnchainTransaction POOL_ACCOUNT_DEPOSIT = new OnchainTransaction(
            TRANSACTION_HASH,
            MODIFICATION_PREFIX + POOL_ACCOUNT_ID + DEPOSIT_SUFFIX,
            OUTPUT_VALUE_2.subtract(INPUT_VALUE_2),
            Coins.NONE
    );

    public static final OnchainTransaction POOL_ACCOUNT_DEPOSIT_WITH_FEES = new OnchainTransaction(
            TRANSACTION_HASH,
            MODIFICATION_PREFIX + POOL_ACCOUNT_ID + DEPOSIT_SUFFIX,
            OUTPUT_VALUE_2.subtract(INPUT_VALUE_2),
            FEES
    );

    public static final Transaction SPEND_TRANSACTION_DETAILS = TRANSACTION;
    public static final OnchainTransaction SPEND_TRANSACTION = new OnchainTransaction(
            TRANSACTION_HASH,
            "",
            Coins.NONE.subtract(OUTPUT_VALUE_2).subtract(FEES),
            SPEND_TRANSACTION_DETAILS.getFees()
    );
}

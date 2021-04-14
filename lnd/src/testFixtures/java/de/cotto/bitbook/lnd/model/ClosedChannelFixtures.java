package de.cotto.bitbook.lnd.model;

import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Input;
import de.cotto.bitbook.backend.transaction.model.Output;
import de.cotto.bitbook.backend.transaction.model.Transaction;

import java.util.List;

import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;

public class ClosedChannelFixtures {
    public static final String BITCOIN_GENESIS_BLOCK_HASH
            = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f";

    public static final Coins CHANNEL_CAPACITY = Coins.ofSatoshis(500);
    public static final String CHANNEL_ADDRESS = "bc1channel";
    public static final String SETTLEMENT_ADDRESS = OUTPUT_ADDRESS_1;
    private static final Coins SETTLED_BALANCE = Coins.ofSatoshis(400);

    public static final Transaction OPENING_TRANSACTION = new Transaction(
            TRANSACTION_HASH,
            BLOCK_HEIGHT,
            DATE_TIME,
            Coins.ofSatoshis(100),
            List.of(
                    new Input(Coins.ofSatoshis(550), INPUT_ADDRESS_1),
                    new Input(Coins.ofSatoshis(50), INPUT_ADDRESS_2)
            ),
            List.of(new Output(CHANNEL_CAPACITY, CHANNEL_ADDRESS))
    );
    public static final Transaction CLOSING_TRANSACTION = new Transaction(
            TRANSACTION_HASH_2,
            BLOCK_HEIGHT,
            DATE_TIME,
            Coins.ofSatoshis(50),
            List.of(new Input(CHANNEL_CAPACITY, CHANNEL_ADDRESS)),
            List.of(new Output(SETTLED_BALANCE, SETTLEMENT_ADDRESS), new Output(Coins.ofSatoshis(50), OUTPUT_ADDRESS_2))
    );
    private static final String REMOTE_PUBKEY = "pubkey";
    private static final Initiator OPEN_INITIATOR = Initiator.REMOTE;

    public static final ClosedChannel CLOSED_CHANNEL = ClosedChannel.builder()
            .withChainHash(BITCOIN_GENESIS_BLOCK_HASH)
            .withOpeningTransaction(OPENING_TRANSACTION)
            .withClosingTransaction(CLOSING_TRANSACTION)
            .withRemotePubkey(REMOTE_PUBKEY)
            .withSettledBalance(SETTLED_BALANCE)
            .withOpenInitiator(OPEN_INITIATOR)
            .withCloseType(CloseType.COOPERATIVE_REMOTE)
            .build();

    public static final ClosedChannel AMBIGUOUS_SETTLEMENT_ADDRESS = CLOSED_CHANNEL.toBuilder()
            .withClosingTransaction(new Transaction(
                    TRANSACTION_HASH_2,
                    BLOCK_HEIGHT,
                    DATE_TIME,
                    Coins.NONE,
                    List.of(new Input(Coins.ofSatoshis(2 * SETTLED_BALANCE.getSatoshis()), CHANNEL_ADDRESS)),
                    List.of(
                            new Output(SETTLED_BALANCE, SETTLEMENT_ADDRESS),
                            new Output(SETTLED_BALANCE, OUTPUT_ADDRESS_2)
                    )
            ))
            .build();
}

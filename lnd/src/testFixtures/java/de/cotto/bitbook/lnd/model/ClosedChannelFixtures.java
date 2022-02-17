package de.cotto.bitbook.lnd.model;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Input;
import de.cotto.bitbook.backend.model.Output;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;

import java.util.List;
import java.util.Set;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_2;

public class ClosedChannelFixtures {
    public static final String BITCOIN_GENESIS_BLOCK_HASH
            = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f";

    public static final Coins CHANNEL_CAPACITY = Coins.ofSatoshis(500);
    public static final Address CHANNEL_ADDRESS = new Address("bc1channel");
    public static final Address SETTLEMENT_ADDRESS = OUTPUT_ADDRESS_1;
    private static final Coins SETTLED_BALANCE = Coins.ofSatoshis(400);

    public static final Transaction OPENING_TRANSACTION = ChannelFixtures.OPENING_TRANSACTION;
    public static final Transaction CLOSING_TRANSACTION = new Transaction(
            TRANSACTION_HASH_2,
            BLOCK_HEIGHT,
            DATE_TIME,
            Coins.ofSatoshis(50),
            List.of(new Input(CHANNEL_CAPACITY, CHANNEL_ADDRESS)),
            List.of(
                    new Output(SETTLED_BALANCE, SETTLEMENT_ADDRESS),
                    new Output(Coins.ofSatoshis(50), OUTPUT_ADDRESS_2)
            ),
            BTC
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
                    ),
                    BTC
            ))
            .build();

    public static final TransactionHash SWEEP_TRANSACTION_HASH = new TransactionHash("sweep_transaction_hash");
    public static final Coins RESOLUTION_AMOUNT = Coins.ofSatoshis(987_654_321L);
    @SuppressWarnings("CPD-START")
    public static final ClosedChannel WITH_RESOLUTION = ClosedChannel.builder()
            .withChainHash(BITCOIN_GENESIS_BLOCK_HASH)
            .withOpeningTransaction(OPENING_TRANSACTION)
            .withClosingTransaction(CLOSING_TRANSACTION)
            .withRemotePubkey(REMOTE_PUBKEY)
            .withSettledBalance(SETTLED_BALANCE)
            .withOpenInitiator(OPEN_INITIATOR)
            .withCloseType(CloseType.COOPERATIVE_REMOTE)
            .withResolutions(Set.of(new Resolution(SWEEP_TRANSACTION_HASH, "COMMIT", "CLAIMED")))
            .build();

    public static final ClosedChannel WITH_RESOLUTION_CLAIMED_OUTGOING_HTLC = ClosedChannel.builder()
            .withChainHash(BITCOIN_GENESIS_BLOCK_HASH)
            .withOpeningTransaction(OPENING_TRANSACTION)
            .withClosingTransaction(CLOSING_TRANSACTION)
            .withRemotePubkey(REMOTE_PUBKEY)
            .withSettledBalance(SETTLED_BALANCE)
            .withOpenInitiator(OPEN_INITIATOR)
            .withCloseType(CloseType.COOPERATIVE_REMOTE)
            .withResolutions(Set.of(new Resolution(SWEEP_TRANSACTION_HASH, "OUTGOING_HTLC", "CLAIMED")))
            .build();

    public static final ClosedChannel WITH_RESOLUTION_TIMEOUT_INCOMING_HTLC = ClosedChannel.builder()
            .withChainHash(BITCOIN_GENESIS_BLOCK_HASH)
            .withOpeningTransaction(OPENING_TRANSACTION)
            .withClosingTransaction(CLOSING_TRANSACTION)
            .withRemotePubkey(REMOTE_PUBKEY)
            .withSettledBalance(SETTLED_BALANCE)
            .withOpenInitiator(OPEN_INITIATOR)
            .withCloseType(CloseType.COOPERATIVE_REMOTE)
            .withResolutions(Set.of(new Resolution(SWEEP_TRANSACTION_HASH, "INCOMING_HTLC", "TIMEOUT")))
            .build();

    public static final ClosedChannel WITH_RESOLUTION_BLANK_HASH = ClosedChannel.builder()
            .withChainHash(BITCOIN_GENESIS_BLOCK_HASH)
            .withOpeningTransaction(OPENING_TRANSACTION)
            .withClosingTransaction(CLOSING_TRANSACTION)
            .withRemotePubkey(REMOTE_PUBKEY)
            .withSettledBalance(SETTLED_BALANCE)
            .withOpenInitiator(OPEN_INITIATOR)
            .withCloseType(CloseType.COOPERATIVE_REMOTE)
            .withResolutions(Set.of(new Resolution(new TransactionHash(" "), "resolutionType", "outcome")))
            .build();
}

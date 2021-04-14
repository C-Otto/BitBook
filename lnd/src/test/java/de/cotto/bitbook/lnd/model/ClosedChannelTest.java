package de.cotto.bitbook.lnd.model;

import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.lnd.model.CloseType.COOPERATIVE_REMOTE;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.AMBIGUOUS_SETTLEMENT_ADDRESS;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.CHANNEL_ADDRESS;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.CLOSING_TRANSACTION;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.OPENING_TRANSACTION;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.SETTLEMENT_ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;

public class ClosedChannelTest {
    @Test
    void invalid_for_other_genesis_block_hash() {
        ClosedChannel closedChannel = CLOSED_CHANNEL.toBuilder().withChainHash("xxx").build();
        assertThat(closedChannel.isValid()).isFalse();
    }

    @Test
    void invalid_for_more_than_one_input() {
        ClosedChannel closedChannel = CLOSED_CHANNEL.toBuilder().withClosingTransaction(TRANSACTION).build();
        assertThat(closedChannel.isValid()).isFalse();
    }

    @Test
    void invalid_for_unknown_open_transaction() {
        ClosedChannel closedChannel = CLOSED_CHANNEL.toBuilder().withOpeningTransaction(Transaction.UNKNOWN).build();
        assertThat(closedChannel.isValid()).isFalse();
    }

    @Test
    void invalid_for_unknown_close_transaction() {
        ClosedChannel closedChannel = CLOSED_CHANNEL.toBuilder().withClosingTransaction(Transaction.UNKNOWN).build();
        assertThat(closedChannel.isValid()).isFalse();
    }

    @Test
    void valid() {
        assertThat(CLOSED_CHANNEL.isValid()).isTrue();
    }

    @Test
    void getChannelAddress() {
        assertThat(CLOSED_CHANNEL.getChannelAddress()).contains(CHANNEL_ADDRESS);
    }

    @Test
    void getSettlementAddress() {
        assertThat(CLOSED_CHANNEL.getSettlementAddress()).contains(SETTLEMENT_ADDRESS);
    }

    @Test
    void getSettlementAddress_two_candidates() {
        assertThat(AMBIGUOUS_SETTLEMENT_ADDRESS.getSettlementAddress()).isEmpty();
    }

    @Test
    void getOpeningTransaction() {
        assertThat(CLOSED_CHANNEL.getOpeningTransaction()).isEqualTo(OPENING_TRANSACTION);
    }

    @Test
    void getClosingTransaction() {
        assertThat(CLOSED_CHANNEL.getClosingTransaction()).isEqualTo(CLOSING_TRANSACTION);
    }

    @Test
    void getRemotePubKey() {
        assertThat(CLOSED_CHANNEL.getRemotePubkey()).isEqualTo("pubkey");
    }

    @Test
    void getSettledBalance() {
        assertThat(CLOSED_CHANNEL.getSettledBalance()).isEqualTo(Coins.ofSatoshis(400));
    }

    @Test
    void getOpenInitiator() {
        assertThat(CLOSED_CHANNEL.getOpenInitiator()).isEqualTo(Initiator.REMOTE);
    }

    @Test
    void getCloseType() {
        assertThat(CLOSED_CHANNEL.getCloseType()).isEqualTo(COOPERATIVE_REMOTE);
    }

    @Test
    void testToString() {
        assertThat(CLOSED_CHANNEL).hasToString(
                "ClosedChannel{" +
                "chainHash='000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f', " +
                "openingTransaction=" + OPENING_TRANSACTION + ", " +
                "closingTransaction=" + CLOSING_TRANSACTION + ", " +
                "remotePubkey='pubkey', " +
                "settledBalance=   0.000004  , " +
                "openInitiator=remote, " +
                "closeType=cooperative remote" +
                "}"
        );
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(ClosedChannel.class).usingGetClass().verify();
    }
}

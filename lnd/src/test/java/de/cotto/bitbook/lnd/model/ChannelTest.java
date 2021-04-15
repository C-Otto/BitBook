package de.cotto.bitbook.lnd.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.lnd.model.ChannelFixtures.CHANNEL;
import static de.cotto.bitbook.lnd.model.ChannelFixtures.CHANNEL_ADDRESS;
import static de.cotto.bitbook.lnd.model.ChannelFixtures.OPENING_TRANSACTION;
import static de.cotto.bitbook.lnd.model.ChannelFixtures.REMOTE_PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;

class ChannelTest {
    @Test
    void isInitiator() {
        assertThat(CHANNEL.isInitiator()).isFalse();
    }

    @Test
    void getRemotePubkey() {
        assertThat(CHANNEL.getRemotePubkey()).isEqualTo(REMOTE_PUBKEY);
    }

    @Test
    void getOpeningTransaction() {
        assertThat(CHANNEL.getOpeningTransaction()).isEqualTo(OPENING_TRANSACTION);
    }

    @Test
    void getChannelAddress() {
        assertThat(CHANNEL.getChannelAddress()).isEqualTo(CHANNEL_ADDRESS);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Channel.class).usingGetClass().verify();
    }
}
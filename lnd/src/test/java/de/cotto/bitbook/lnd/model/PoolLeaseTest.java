package de.cotto.bitbook.lnd.model;

import de.cotto.bitbook.backend.model.Coins;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.lnd.model.PoolLeaseFixtures.POOL_LEASE;
import static de.cotto.bitbook.lnd.model.PoolLeaseFixtures.POOL_LEASE_2;
import static de.cotto.bitbook.lnd.model.PoolLeaseFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;

class PoolLeaseTest {
    @Test
    void getTransactionHash() {
        assertThat(POOL_LEASE.getTransactionHash()).isEqualTo(TRANSACTION_HASH);
    }

    @Test
    void getOutputIndex() {
        assertThat(POOL_LEASE_2.getOutputIndex()).isEqualTo(1);
    }

    @Test
    void getPubKey() {
        assertThat(POOL_LEASE.getPubKey()).isEqualTo(PUBKEY);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(PoolLease.class).usingGetClass().verify();
    }

    @Test
    void getPremiumWithoutFees() {
        assertThat(POOL_LEASE.getPremiumWithoutFees()).isEqualTo(Coins.ofSatoshis(1500 - 150 - 114));
    }
}

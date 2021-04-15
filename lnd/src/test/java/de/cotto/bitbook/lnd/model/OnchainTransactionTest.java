package de.cotto.bitbook.lnd.model;

import de.cotto.bitbook.backend.transaction.model.Coins;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.ONCHAIN_TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;

class OnchainTransactionTest {

    @Test
    void getTransactionHash() {
        assertThat(ONCHAIN_TRANSACTION.getTransactionHash()).isEqualTo(TRANSACTION_HASH);
    }

    @Test
    void getLabel() {
        assertThat(ONCHAIN_TRANSACTION.getLabel()).isEqualTo("label");
    }

    @Test
    void getAmount() {
        assertThat(ONCHAIN_TRANSACTION.getAmount()).isEqualTo(Coins.ofSatoshis(500));
    }

    @Test
    void getFees() {
        assertThat(ONCHAIN_TRANSACTION.getFees()).isEqualTo(Coins.ofSatoshis(100));
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(OnchainTransaction.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        assertThat(ONCHAIN_TRANSACTION)
                .hasToString("OnchainTransaction{" +
                             "transactionHash='" + TRANSACTION_HASH + "'" +
                             ", label='label'" +
                             ", amount=" + Coins.ofSatoshis(500) +
                             ", fees=" + Coins.ofSatoshis(100) +
                             "}");
    }
}
package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.TransactionHash;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static de.cotto.bitbook.backend.transaction.TransactionRequestFixtures.TRANSACTION_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

class TransactionRequestTest {

    @Test
    void getTransactionHash_standard() {
        assertThat(new TransactionRequest(TRANSACTION_HASH, STANDARD).getHash()).isEqualTo(TRANSACTION_HASH);
    }

    @Test
    void getTransactionHash_lowest() {
        assertThat(new TransactionRequest(TRANSACTION_HASH, LOWEST).getHash()).isEqualTo(TRANSACTION_HASH);
    }

    @Test
    void getRequestPriority_standard() {
        assertThat(new TransactionRequest(TRANSACTION_HASH, STANDARD).getPriority()).isEqualTo(STANDARD);
    }

    @Test
    void getRequestPriority_lowest() {
        assertThat(new TransactionRequest(TRANSACTION_HASH, LOWEST).getPriority()).isEqualTo(LOWEST);
    }

    @Test
    void testEquals() {
        assertThat(TRANSACTION_REQUEST).isEqualTo(new TransactionRequest(TRANSACTION_HASH, STANDARD));
    }

    @Test
    void testEquals_different_priority() {
        assertThat(TRANSACTION_REQUEST)
                .isNotEqualTo(new TransactionRequest(TRANSACTION_HASH, LOWEST));
    }

    @Test
    void testEquals_different_hash() {
        assertThat(TRANSACTION_REQUEST).isNotEqualTo(new TransactionRequest(new TransactionHash("xxx"), STANDARD));
    }

    @Test
    void testToString() {
        assertThat(TRANSACTION_REQUEST).hasToString(
                "TransactionRequest{" +
                "transactionHash='c56c2a4ec7099879c2c4da74f4e5105a5a5d0ed94aa7d64518fa7e4256d42d9e'" +
                ", priority=STANDARD" +
                "}");
    }
}
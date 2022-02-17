package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.HashAndChain;
import de.cotto.bitbook.backend.model.TransactionHash;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static de.cotto.bitbook.backend.transaction.TransactionRequestFixtures.TRANSACTION_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

class TransactionRequestTest {

    @Test
    void getHashAndChain_standard() {
        assertThat(new TransactionRequest(TRANSACTION_HASH, BTC, STANDARD).getHashAndChain())
                .isEqualTo(new HashAndChain(TRANSACTION_HASH, BTC));
    }

    @Test
    void getHashAndChain_lowest() {
        assertThat(new TransactionRequest(TRANSACTION_HASH, BCH, LOWEST).getHashAndChain())
                .isEqualTo(new HashAndChain(TRANSACTION_HASH, BCH));
    }

    @Test
    void getRequestPriority_standard() {
        assertThat(new TransactionRequest(TRANSACTION_HASH, BTC, STANDARD).getPriority()).isEqualTo(STANDARD);
    }

    @Test
    void getRequestPriority_lowest() {
        assertThat(new TransactionRequest(TRANSACTION_HASH, BTC, LOWEST).getPriority()).isEqualTo(LOWEST);
    }

    @Test
    void testEquals() {
        assertThat(TRANSACTION_REQUEST).isEqualTo(new TransactionRequest(TRANSACTION_HASH, BTC, STANDARD));
    }

    @Test
    void testEquals_different_priority() {
        assertThat(TRANSACTION_REQUEST)
                .isNotEqualTo(new TransactionRequest(TRANSACTION_HASH, BTC, LOWEST));
    }

    @Test
    void testEquals_different_hash() {
        assertThat(TRANSACTION_REQUEST).isNotEqualTo(new TransactionRequest(new TransactionHash("xxx"), BTC, STANDARD));
    }

    @Test
    void testEquals_different_chain() {
        assertThat(TRANSACTION_REQUEST).isNotEqualTo(new TransactionRequest(TRANSACTION_HASH, BCH, STANDARD));
    }
}
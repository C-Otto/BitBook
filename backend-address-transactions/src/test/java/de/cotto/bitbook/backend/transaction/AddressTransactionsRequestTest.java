package de.cotto.bitbook.backend.transaction;

import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static de.cotto.bitbook.backend.transaction.TransactionsRequestKeyFixtures.ADDRESS_TRANSACTIONS_LOWEST;
import static de.cotto.bitbook.backend.transaction.TransactionsRequestKeyFixtures.ADDRESS_TRANSACTIONS_REQUEST;
import static de.cotto.bitbook.backend.transaction.TransactionsRequestKeyFixtures.TRANSACTIONS_REQUEST_KEY;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static org.assertj.core.api.Assertions.assertThat;

class AddressTransactionsRequestTest {
    @Test
    void getKey_standard() {
        assertThat(ADDRESS_TRANSACTIONS_REQUEST.getKey()).isEqualTo(TRANSACTIONS_REQUEST_KEY);
    }

    @Test
    void getKey_lowest() {
        assertThat(ADDRESS_TRANSACTIONS_LOWEST.getKey()).isEqualTo(TRANSACTIONS_REQUEST_KEY);
    }

    @Test
    void getRequestPriority_standard() {
        assertThat(ADDRESS_TRANSACTIONS_REQUEST.getPriority()).isEqualTo(STANDARD);
    }

    @Test
    void getRequestPriority_lowest() {
        assertThat(ADDRESS_TRANSACTIONS_LOWEST.getPriority()).isEqualTo(LOWEST);
    }

    @Test
    void testEquals() {
        AddressTransactionsRequest request = AddressTransactionsRequest.forStandardPriority(TRANSACTIONS_REQUEST_KEY);
        assertThat(ADDRESS_TRANSACTIONS_REQUEST).isEqualTo(request);
    }

    @Test
    void testEquals_different_priority() {
        assertThat(ADDRESS_TRANSACTIONS_LOWEST).isNotEqualTo(ADDRESS_TRANSACTIONS_REQUEST);
    }

    @Test
    void testEquals_different_address() {
        assertThat(ADDRESS_TRANSACTIONS_REQUEST).isNotEqualTo(
                AddressTransactionsRequest.forStandardPriority(new TransactionsRequestKey("xxx", BLOCK_HEIGHT))
        );
    }

    @Test
    void testToString() {
        assertThat(ADDRESS_TRANSACTIONS_REQUEST).hasToString(
                "AddressTransactionsRequest{" +
                "transactionsRequestKey='" + TRANSACTIONS_REQUEST_KEY + "'" +
                ", priority=STANDARD" +
                "}");
    }
}
package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Address;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static de.cotto.bitbook.backend.transaction.TransactionsRequestKeyFixtures.ADDRESS_TRANSACTIONS_LOWEST;
import static de.cotto.bitbook.backend.transaction.TransactionsRequestKeyFixtures.ADDRESS_TRANSACTIONS_REQUEST;
import static de.cotto.bitbook.backend.transaction.TransactionsRequestKeyFixtures.TRANSACTIONS_REQUEST_KEY;
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
        AddressTransactionsRequest request = AddressTransactionsRequest.create(TRANSACTIONS_REQUEST_KEY, STANDARD);
        assertThat(ADDRESS_TRANSACTIONS_REQUEST).isEqualTo(request);
    }

    @Test
    void testEquals_different_priority() {
        assertThat(ADDRESS_TRANSACTIONS_LOWEST).isNotEqualTo(ADDRESS_TRANSACTIONS_REQUEST);
    }

    @Test
    void testEquals_different_address() {
        TransactionsRequestKey key = new TransactionsRequestKey(new Address("xxx"), BLOCK_HEIGHT);
        assertThat(ADDRESS_TRANSACTIONS_REQUEST).isNotEqualTo(AddressTransactionsRequest.create(key, STANDARD));
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
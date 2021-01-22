package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static de.cotto.bitbook.backend.transaction.TransactionsRequestKeyFixtures.ADDRESS_TRANSACTIONS_LOWEST;
import static de.cotto.bitbook.backend.transaction.TransactionsRequestKeyFixtures.ADDRESS_TRANSACTIONS_REQUEST;
import static de.cotto.bitbook.backend.transaction.TransactionsRequestKeyFixtures.TRANSACTIONS_REQUEST_KEY;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class AddressTransactionsRequestTest {

    @Nullable
    private AddressTransactions seen;

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
    void default_result_consumer_does_nothing() {
        assertThatCode(() ->
            ADDRESS_TRANSACTIONS_LOWEST.getWithResultFuture().provideResult(ADDRESS_TRANSACTIONS)
        ).doesNotThrowAnyException();
    }

    @Test
    void getWithResultConsumer() {
        AddressTransactionsRequest requestWithResultConsumer =
                ADDRESS_TRANSACTIONS_LOWEST.getWithResultConsumer(this::resultConsumer);
        requestWithResultConsumer.getWithResultFuture().provideResult(ADDRESS_TRANSACTIONS);

        assertThat(requestWithResultConsumer.getKey()).isEqualTo(TRANSACTIONS_REQUEST_KEY);
        assertThat(seen).isEqualTo(ADDRESS_TRANSACTIONS);
    }

    private void resultConsumer(AddressTransactions result) {
        seen = result;
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
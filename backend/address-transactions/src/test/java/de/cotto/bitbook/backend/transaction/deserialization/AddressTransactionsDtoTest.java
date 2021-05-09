package de.cotto.bitbook.backend.transaction.deserialization;

import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AddressTransactionsDtoTest {
    public static final String ADDRESS_3 = "abc";

    @Test
    void toModel() {
        AddressTransactionsDto dto = new AddressTransactionsDto(ADDRESS_3, Set.of(ADDRESS, ADDRESS_2));
        AddressTransactions addressTransactions =
                new AddressTransactions(ADDRESS_3, Set.of(ADDRESS, ADDRESS_2), 123);
        assertThat(dto.toModel(123, ADDRESS_3)).isEqualTo(addressTransactions);
    }

    @Test
    void toModel_invalid_address() {
        AddressTransactionsDto dto = new AddressTransactionsDto(ADDRESS_3, Set.of(ADDRESS, ADDRESS_2));
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> dto.toModel(123, "def"));
    }

    @Test
    void getAddress() {
        assertThat(new AddressTransactionsDto(ADDRESS_3, Set.of(ADDRESS, ADDRESS_2)).getAddress()).isEqualTo(ADDRESS_3);
    }

    @Test
    void getTransactionHashes() {
        assertThat(new AddressTransactionsDto(ADDRESS_3, Set.of(ADDRESS, ADDRESS_2)).getTransactionHashes())
                .containsExactlyInAnyOrder(ADDRESS, ADDRESS_2);
    }
}
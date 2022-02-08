package de.cotto.bitbook.backend.transaction.deserialization;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_3;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AddressTransactionsDtoTest {
    @Test
    void toModel() {
        AddressTransactionsDto dto =
                new AddressTransactionsDto(ADDRESS_3, Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2));
        AddressTransactions addressTransactions =
                new AddressTransactions(ADDRESS_3, Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2), 123);
        assertThat(dto.toModel(123, ADDRESS_3)).isEqualTo(addressTransactions);
    }

    @Test
    void toModel_invalid_address() {
        AddressTransactionsDto dto =
                new AddressTransactionsDto(ADDRESS_3, Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2));
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> dto.toModel(123, new Address("def")));
    }

    @Test
    void getAddress() {
        AddressTransactionsDto dto =
                new AddressTransactionsDto(ADDRESS_3, Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2));
        assertThat(dto.getAddress()).isEqualTo(ADDRESS_3);
    }

    @Test
    void getTransactionHashes() {
        AddressTransactionsDto dto =
                new AddressTransactionsDto(ADDRESS_3, Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2));
        assertThat(dto.getTransactionHashes()).containsExactlyInAnyOrder(TRANSACTION_HASH, TRANSACTION_HASH_2);
    }
}
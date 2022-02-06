package de.cotto.bitbook.backend.transaction.persistence;

import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.transaction.persistence.AddressTransactionsJpaDtoFixtures.ADDRESS_TRANSACTIONS_JPA_DTO;
import static org.assertj.core.api.Assertions.assertThat;

class AddressTransactionsJpaDtoTest {
    @Test
    void toModel() {
        assertThat(ADDRESS_TRANSACTIONS_JPA_DTO.toModel()).isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void fromModel() {
        assertThat(AddressTransactionsJpaDto.fromModel(ADDRESS_TRANSACTIONS))
                .usingRecursiveComparison().isEqualTo(ADDRESS_TRANSACTIONS_JPA_DTO);
    }
}
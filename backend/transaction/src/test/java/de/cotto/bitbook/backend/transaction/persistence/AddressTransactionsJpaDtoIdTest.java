package de.cotto.bitbook.backend.transaction.persistence;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static org.assertj.core.api.Assertions.assertThat;

class AddressTransactionsJpaDtoIdTest {
    @Test
    void fromModel() {
        assertThat(AddressTransactionsJpaDtoId.fromModels(ADDRESS, BTC))
                .isEqualTo(new AddressTransactionsJpaDtoId(ADDRESS.toString(), "BTC"));
    }

    @Test
    void testEquals() {
        EqualsVerifier.simple().forClass(AddressTransactionsJpaDtoId.class).verify();
    }
}
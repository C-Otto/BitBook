package de.cotto.bitbook.ownership.persistence;

import de.cotto.bitbook.ownership.OwnershipStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressOwnershipJpaDtoTest {

    @Test
    void getAddress() {
        AddressOwnershipJpaDto addressOwnershipJpaDto = new AddressOwnershipJpaDto("x", OwnershipStatus.OWNED);
        assertThat(addressOwnershipJpaDto.getAddress()).isEqualTo("x");
    }

    @Test
    void getOwnershipStatus_not_set() {
        assertThat(new AddressOwnershipJpaDto().getOwnershipStatus()).isEqualTo(OwnershipStatus.UNKNOWN);
    }

    @Test
    void getOwnershipStatus() {
        assertThat(new AddressOwnershipJpaDto("x", OwnershipStatus.FOREIGN).getOwnershipStatus())
                .isEqualTo(OwnershipStatus.FOREIGN);
    }
}
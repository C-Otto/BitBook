package de.cotto.bitbook.ownership.persistence;

import de.cotto.bitbook.backend.model.Address;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.ownership.OwnershipStatus.OWNED;
import static de.cotto.bitbook.ownership.OwnershipStatus.UNKNOWN;
import static de.cotto.bitbook.ownership.persistence.AddressOwnershipJpaDtoFixtures.FOREIGN_ADDRESS_JPA_DTO_1;
import static de.cotto.bitbook.ownership.persistence.AddressOwnershipJpaDtoFixtures.FOREIGN_ADDRESS_JPA_DTO_2;
import static de.cotto.bitbook.ownership.persistence.AddressOwnershipJpaDtoFixtures.OWNED_ADDRESS_JPA_DTO_1;
import static de.cotto.bitbook.ownership.persistence.AddressOwnershipJpaDtoFixtures.OWNED_ADDRESS_JPA_DTO_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressOwnershipDaoTest {
    @InjectMocks
    private AddressOwnershipDaoImpl ownedAddressesDao;

    @Mock
    private AddressOwnershipRepository addressOwnershipRepository;

    @Test
    void getOwnedAddresses() {
        when(addressOwnershipRepository.findAll())
                .thenReturn(List.of(OWNED_ADDRESS_JPA_DTO_1, FOREIGN_ADDRESS_JPA_DTO_1, OWNED_ADDRESS_JPA_DTO_2));
        assertThat(ownedAddressesDao.getOwnedAddresses()).containsExactlyInAnyOrder(
                new Address(OWNED_ADDRESS_JPA_DTO_1.getAddress()),
                new Address(OWNED_ADDRESS_JPA_DTO_2.getAddress())
        );
    }

    @Test
    void getForeignAddresses() {
        when(addressOwnershipRepository.findAll())
                .thenReturn(List.of(OWNED_ADDRESS_JPA_DTO_1, FOREIGN_ADDRESS_JPA_DTO_1, FOREIGN_ADDRESS_JPA_DTO_2));
        assertThat(ownedAddressesDao.getForeignAddresses()).containsExactlyInAnyOrder(
                new Address(FOREIGN_ADDRESS_JPA_DTO_1.getAddress()),
                new Address(FOREIGN_ADDRESS_JPA_DTO_2.getAddress())
        );
    }

    @Test
    void setAddressAsOwned() {
        ownedAddressesDao.setAddressAsOwned(new Address(OWNED_ADDRESS_JPA_DTO_1.getAddress()));
        verify(addressOwnershipRepository).save(
                argThat(dto -> OWNED_ADDRESS_JPA_DTO_1.getAddress().equals(dto.getAddress()))
        );
        verify(addressOwnershipRepository).save(
                argThat(dto -> OWNED_ADDRESS_JPA_DTO_1.getOwnershipStatus().equals(dto.getOwnershipStatus()))
        );
    }

    @Test
    void setAddressAsForeign() {
        ownedAddressesDao.setAddressAsForeign(new Address(FOREIGN_ADDRESS_JPA_DTO_1.getAddress()));
        verify(addressOwnershipRepository).save(
                argThat(dto -> FOREIGN_ADDRESS_JPA_DTO_1.getAddress().equals(dto.getAddress()))
        );
        verify(addressOwnershipRepository).save(
                argThat(dto -> FOREIGN_ADDRESS_JPA_DTO_1.getOwnershipStatus().equals(dto.getOwnershipStatus()))
        );
    }

    @Test
    void remove() {
        ownedAddressesDao.remove(ADDRESS);
        verify(addressOwnershipRepository).deleteById(ADDRESS.toString());
    }

    @Test
    void getOwnershipStatus_unknown() {
        assertThat(ownedAddressesDao.getOwnershipStatus(ADDRESS)).isEqualTo(UNKNOWN);
    }

    @Test
    void getOwnershipStatus() {
        when(addressOwnershipRepository.findByAddress(ADDRESS.toString()))
                .thenReturn(Optional.of(new AddressOwnershipJpaDto(ADDRESS.toString(), OWNED)));
        assertThat(ownedAddressesDao.getOwnershipStatus(ADDRESS)).isEqualTo(OWNED);
    }
}
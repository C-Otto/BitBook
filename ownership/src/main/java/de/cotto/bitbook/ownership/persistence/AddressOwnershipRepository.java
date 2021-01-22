package de.cotto.bitbook.ownership.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressOwnershipRepository extends JpaRepository<AddressOwnershipJpaDto, String> {
    Optional<AddressOwnershipJpaDto> findByAddress(String address);
}

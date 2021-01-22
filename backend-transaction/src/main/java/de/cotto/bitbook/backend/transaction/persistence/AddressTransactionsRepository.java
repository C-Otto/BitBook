package de.cotto.bitbook.backend.transaction.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressTransactionsRepository extends JpaRepository<AddressTransactionsJpaDto, String> {
    List<AddressView> findByAddressStartingWith(String prefix);
}

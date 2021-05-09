package de.cotto.bitbook.backend.transaction.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface InputRepository extends JpaRepository<InputJpaDto, String> {
    Set<InputJpaDto> findBySourceAddressStartingWith(String prefix);
}

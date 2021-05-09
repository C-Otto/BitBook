package de.cotto.bitbook.backend.transaction.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface OutputRepository extends JpaRepository<OutputJpaDto, String> {
    Set<OutputJpaDto> findByTargetAddressStartingWith(String prefix);
}

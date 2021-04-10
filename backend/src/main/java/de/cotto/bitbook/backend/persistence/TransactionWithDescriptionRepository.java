package de.cotto.bitbook.backend.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface TransactionWithDescriptionRepository extends JpaRepository<TransactionWithDescriptionJpaDto, String> {
    Set<TransactionWithDescriptionJpaDto> findByDescriptionContaining(String infix);
}

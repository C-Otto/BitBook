package de.cotto.bitbook.backend.transaction.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface TransactionRepository extends JpaRepository<TransactionJpaDto, TransactionJpaDtoId> {
    Set<TransactionHashView> findByHashStartingWith(String hashPrefix);
}

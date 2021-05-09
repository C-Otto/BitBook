package de.cotto.bitbook.backend.transaction.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface AddressTransactionsRepository extends JpaRepository<AddressTransactionsJpaDto, String> {
    List<AddressView> findByAddressStartingWith(String prefix);

    @Query("SELECT hash FROM AddressTransactionsJpaDto addressTransactions INNER JOIN " +
           "addressTransactions.transactionHashes hash WHERE hash LIKE :hashPrefix%")
    Set<String> findTransactionHashesByPrefix(String hashPrefix);
}

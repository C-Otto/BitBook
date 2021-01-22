package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.transaction.model.Transaction;

import java.util.Set;

public interface TransactionDao {
    Transaction getTransaction(String transactionHash);

    void saveTransaction(Transaction transaction);

    Set<String> getTransactionHashesStartingWith(String hashPrefix);
}

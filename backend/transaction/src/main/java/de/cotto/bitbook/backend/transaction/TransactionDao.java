package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Transaction;

public interface TransactionDao {
    Transaction getTransaction(String transactionHash);

    void saveTransaction(Transaction transaction);
}

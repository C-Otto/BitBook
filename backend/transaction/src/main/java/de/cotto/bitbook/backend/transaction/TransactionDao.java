package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;

public interface TransactionDao {
    Transaction getTransaction(TransactionHash transactionHash, Chain chain);

    void saveTransaction(Transaction transaction);
}

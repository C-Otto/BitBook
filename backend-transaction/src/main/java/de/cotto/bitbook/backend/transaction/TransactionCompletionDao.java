package de.cotto.bitbook.backend.transaction;

import java.util.Set;

public interface TransactionCompletionDao {
    Set<String> getTransactionHashesStartingWith(String hashPrefix);
}

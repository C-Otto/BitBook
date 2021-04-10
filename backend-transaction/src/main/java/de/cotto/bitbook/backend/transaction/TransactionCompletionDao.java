package de.cotto.bitbook.backend.transaction;

import java.util.Set;

public interface TransactionCompletionDao {
    Set<String> completeFromTransactionDetails(String hashPrefix);

    Set<String> completeFromAddressTransactionHashes(String hashPrefix);
}

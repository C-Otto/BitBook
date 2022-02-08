package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.TransactionHash;

import java.util.Set;

public interface TransactionCompletionDao {
    Set<TransactionHash> completeFromTransactionDetails(String hashPrefix);

    Set<TransactionHash> completeFromAddressTransactionHashes(String hashPrefix);
}

package de.cotto.bitbook.backend;

import de.cotto.bitbook.backend.model.TransactionWithDescription;

import java.util.Set;

public interface TransactionWithDescriptionDao {
    TransactionWithDescription get(String transactionHash);

    void save(TransactionWithDescription transactionWithDescription);

    Set<TransactionWithDescription> findWithDescriptionInfix(String infix);

    void remove(String transactionHash);
}

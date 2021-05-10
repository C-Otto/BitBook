package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;

@Component
public class TransactionSorter {

    private static final Comparator<Map.Entry<Transaction, Coins>> BY_HASH =
            Comparator.comparing(entry -> entry.getKey().getHash());

    private static final Comparator<Map.Entry<Transaction, Coins>> BY_ABSOLUTE_COINS =
            Comparator.comparing(entry -> entry.getValue().absolute());

    private static final Comparator<Map.Entry<Transaction, Coins>> BY_ABSOLUTE_COINS_THEN_HASH =
            BY_ABSOLUTE_COINS.thenComparing(BY_HASH);

    public TransactionSorter() {
        // default constructor
    }

    public Comparator<Map.Entry<Transaction, Coins>> getComparator() {
        return BY_ABSOLUTE_COINS_THEN_HASH;
    }
}

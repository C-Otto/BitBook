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

    private static final Comparator<Map.Entry<Transaction, Coins>> BY_DATE =
            Comparator.comparing((Map.Entry<Transaction, Coins> entry) -> entry.getKey().getBlockHeight())
                    .thenComparing((Map.Entry<Transaction, Coins> entry) -> entry.getKey().getTime());

    private static final Comparator<Map.Entry<Transaction, Coins>> BY_COINS =
            Map.Entry.comparingByValue();

    private static final Comparator<Map.Entry<Transaction, Coins>> BY_COINS_ABSOLUTE =
            Comparator.comparing(entry -> entry.getValue().absolute());

    private static final Comparator<Map.Entry<Transaction, Coins>> BY_COINS_ABSOLUTE_THEN_HASH =
            BY_COINS_ABSOLUTE.thenComparing(BY_HASH);

    private static final Comparator<Map.Entry<Transaction, Coins>> BY_COINS_THEN_HASH =
            BY_COINS.thenComparing(BY_HASH);

    private static final Comparator<Map.Entry<Transaction, Coins>> BY_DATE_THEN_COINS_HASH =
            BY_DATE.thenComparing(BY_COINS_THEN_HASH);

    private static final Comparator<Map.Entry<Transaction, Coins>> BY_DATE_THEN_COINS_ABSOLUTE_HASH =
            BY_DATE.thenComparing(BY_COINS_ABSOLUTE_THEN_HASH);

    private static final Comparator<Map.Entry<Transaction, Coins>> BY_DATE_THEN_HASH =
            BY_DATE.thenComparing(BY_HASH);

    private static final Comparator<Map.Entry<Transaction, Coins>> BY_COINS_ABSOLUTE_THEN_DATE_HASH =
            BY_COINS_ABSOLUTE.thenComparing(BY_DATE_THEN_HASH);

    private static final Comparator<Map.Entry<Transaction, Coins>> BY_COINS_THEN_DATE_HASH =
            BY_COINS.thenComparing(BY_DATE_THEN_HASH);

    private static final Comparator<Map.Entry<Transaction, Coins>> DEFAULT_COMPARATOR =
            BY_COINS_ABSOLUTE_THEN_DATE_HASH;

    private Comparator<Map.Entry<Transaction, Coins>> comparator;

    public TransactionSorter() {
        this.comparator = DEFAULT_COMPARATOR;
    }

    public void setOrder(TransactionSortOrder transactionSortOrder) {
        //noinspection EnhancedSwitchMigration
        switch (transactionSortOrder) {
            case BY_HASH:
                comparator = BY_HASH;
                break;
            case BY_DATE_THEN_COINS_THEN_HASH:
                comparator = BY_DATE_THEN_COINS_HASH;
                break;
            case BY_DATE_THEN_COINS_ABSOLUTE_THEN_HASH:
                comparator = BY_DATE_THEN_COINS_ABSOLUTE_HASH;
                break;
            case BY_COINS_THEN_HASH:
                comparator = BY_COINS_THEN_HASH;
                break;
            case BY_COINS_THEN_DATE_THEN_HASH:
                comparator = BY_COINS_THEN_DATE_HASH;
                break;
            case BY_COINS_ABSOLUTE_THEN_DATE_THEN_HASH:
                comparator = BY_COINS_ABSOLUTE_THEN_DATE_HASH;
                break;
            case BY_DATE_THEN_HASH:
                comparator = BY_DATE_THEN_HASH;
                break;
            case BY_COINS_ABSOLUTE_THEN_HASH:
                comparator = BY_COINS_ABSOLUTE_THEN_HASH;
                break;
            default:
                comparator = DEFAULT_COMPARATOR;
                break;
        }
    }

    public Comparator<Map.Entry<Transaction, Coins>> getComparator() {
        return comparator;
    }
}

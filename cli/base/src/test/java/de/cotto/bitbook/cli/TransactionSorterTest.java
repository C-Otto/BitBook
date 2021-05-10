package de.cotto.bitbook.cli;

import com.google.common.collect.Lists;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_3;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TransactionSorterTest {
    @InjectMocks
    private TransactionSorter transactionSorter;

    @Test
    void comparesByCoinsFirst() {
        List<Map.Entry<Transaction, Coins>> list = Lists.newArrayList(
                entry(TRANSACTION, Coins.NONE),
                entry(TRANSACTION_3, Coins.ofSatoshis(2)),
                entry(TRANSACTION_2, Coins.ofSatoshis(1))
        );
        list.sort(transactionSorter.getComparator());
        assertThat(list).containsExactly(
                entry(TRANSACTION, Coins.NONE),
                entry(TRANSACTION_2, Coins.ofSatoshis(1)),
                entry(TRANSACTION_3, Coins.ofSatoshis(2))
        );
    }

    @Test
    void comparesByAbsoluteValue() {
        List<Map.Entry<Transaction, Coins>> list = Lists.newArrayList(
                entry(TRANSACTION_3, Coins.ofSatoshis(-2)),
                entry(TRANSACTION_2, Coins.ofSatoshis(1))
        );
        list.sort(transactionSorter.getComparator());
        assertThat(list).containsExactly(
                entry(TRANSACTION_2, Coins.ofSatoshis(1)),
                entry(TRANSACTION_3, Coins.ofSatoshis(-2))
        );
    }

    @Test
    void comparesByHashForSameValue() {
        List<Map.Entry<Transaction, Coins>> list = Lists.newArrayList(
                entry(TRANSACTION, Coins.NONE),
                entry(TRANSACTION_2, Coins.NONE)
        );
        list.sort(transactionSorter.getComparator());
        assertThat(list).containsExactly(
                entry(TRANSACTION_2, Coins.NONE),
                entry(TRANSACTION, Coins.NONE)
        );
    }

    @Test
    void comparesByHashForSameAbsoluteValue() {
        List<Map.Entry<Transaction, Coins>> list = Lists.newArrayList(
                entry(TRANSACTION_3, Coins.ofSatoshis(1)),
                entry(TRANSACTION_2, Coins.ofSatoshis(-1))
        );
        list.sort(transactionSorter.getComparator());
        assertThat(list).containsExactly(
                entry(TRANSACTION_3, Coins.ofSatoshis(1)),
                entry(TRANSACTION_2, Coins.ofSatoshis(-1))
        );
    }
}
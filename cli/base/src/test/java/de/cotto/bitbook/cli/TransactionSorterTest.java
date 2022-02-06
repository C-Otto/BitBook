package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionFixtures;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_2;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_3;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_4;
import static de.cotto.bitbook.cli.TransactionSortOrder.BY_COINS_ABSOLUTE_THEN_DATE_THEN_HASH;
import static de.cotto.bitbook.cli.TransactionSortOrder.BY_COINS_ABSOLUTE_THEN_HASH;
import static de.cotto.bitbook.cli.TransactionSortOrder.BY_COINS_THEN_DATE_THEN_HASH;
import static de.cotto.bitbook.cli.TransactionSortOrder.BY_COINS_THEN_HASH;
import static de.cotto.bitbook.cli.TransactionSortOrder.BY_DATE_THEN_COINS_ABSOLUTE_THEN_HASH;
import static de.cotto.bitbook.cli.TransactionSortOrder.BY_DATE_THEN_COINS_THEN_HASH;
import static de.cotto.bitbook.cli.TransactionSortOrder.BY_DATE_THEN_HASH;
import static java.util.Map.entry;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class TransactionSorterTest {
    private static final Coins COINS = Coins.ofSatoshis(123);
    private static final Coins COINS_NEGATIVE = Coins.ofSatoshis(-123);

    private final TransactionSorter transactionSorter = new TransactionSorter();

    private final SoftAssertions softly = new SoftAssertions();

    @Nested
    class SameDateSameCoins {
        @Test
        void sortsByHash() {
            Map.Entry<Transaction, Coins> hash3 = entry(TRANSACTION, COINS);
            Map.Entry<Transaction, Coins> hash1 = entry(TRANSACTION_3, COINS);
            Map.Entry<Transaction, Coins> hash2 = entry(TRANSACTION_4, COINS);
            ArrayList<Map.Entry<Transaction, Coins>> entries = Lists.newArrayList(hash3, hash1, hash2);

            for (TransactionSortOrder order : TransactionSortOrder.values()) {
                assertOrder(entries, order, hash1, hash2, hash3);
            }
            softly.assertAll();
        }
    }

    @Nested
    class SameDateSameAbsoluteCoins {
        private final Map.Entry<Transaction, Coins> hash3coins2 = entry(TRANSACTION, COINS);
        private final Map.Entry<Transaction, Coins> hash1coins2 = entry(TRANSACTION_3, COINS);
        private final Map.Entry<Transaction, Coins> hash2coins1 = entry(TRANSACTION_4, COINS_NEGATIVE);
        private final Set<TransactionSortOrder> sortByCoins =
                Set.of(BY_COINS_THEN_HASH, BY_COINS_THEN_DATE_THEN_HASH, BY_DATE_THEN_COINS_THEN_HASH);
        private final List<Map.Entry<Transaction, Coins>> entries =
                Lists.newArrayList(hash3coins2, hash1coins2, hash2coins1);

        @Test
        void sortsByHash() {
            for (TransactionSortOrder order : TransactionSortOrder.values()) {
                if (sortByCoins.contains(order)) {
                    continue;
                }
                assertOrder(entries, order, hash1coins2, hash2coins1, hash3coins2);
            }
            softly.assertAll();
        }

        @Test
        void sortsByCoinsThenHash() {
            for (TransactionSortOrder order : sortByCoins) {
                assertOrder(entries, order, hash2coins1, hash1coins2, hash3coins2);
            }
            softly.assertAll();
        }
    }

    @Nested
    class SameCoinsDifferentDate {
        private final Map.Entry<Transaction, Coins> date1hash2 = entry(TRANSACTION, COINS);
        private final Map.Entry<Transaction, Coins> date2hash1 = entry(TRANSACTION_2, COINS);
        private final List<Map.Entry<Transaction, Coins>> entries = Lists.newArrayList(date1hash2, date2hash1);
        private final Set<TransactionSortOrder> sortByDate = Set.of(
                BY_DATE_THEN_HASH,
                BY_DATE_THEN_COINS_THEN_HASH,
                BY_DATE_THEN_COINS_ABSOLUTE_THEN_HASH,
                BY_COINS_THEN_DATE_THEN_HASH,
                BY_COINS_ABSOLUTE_THEN_DATE_THEN_HASH
        );

        @Test
        void sortsByHash() {
            for (TransactionSortOrder order : TransactionSortOrder.values()) {
                if (sortByDate.contains(order)) {
                    continue;
                }
                assertOrder(entries, order, date2hash1, date1hash2);
            }
            softly.assertAll();
        }

        @Test
        void sortsByDate() {
            for (TransactionSortOrder order : sortByDate) {
                assertOrder(entries, order, date1hash2, date2hash1);
            }
            softly.assertAll();
        }

        @Test
        void sortsByBlockThenDate() {
            Transaction earlierBlockTransaction = new Transaction(
                    TransactionFixtures.TRANSACTION_HASH_2,
                    TransactionFixtures.BLOCK_HEIGHT - 1,
                    TransactionFixtures.DATE_TIME.plusDays(1),
                    Coins.NONE,
                    List.of(),
                    List.of()
            );
            Map.Entry<Transaction, Coins> block1date2 = entry(earlierBlockTransaction, COINS);
            Map.Entry<Transaction, Coins> block2date1 = entry(TRANSACTION, COINS);
            for (TransactionSortOrder order : sortByDate) {
                assertOrder(Lists.newArrayList(block2date1, block1date2), order, block1date2, block2date1);
            }
            softly.assertAll();
        }
    }

    @Nested
    class SameAbsoluteCoinsDifferentDate {
        private final Map.Entry<Transaction, Coins> date1coins2hash2 = entry(TRANSACTION, COINS);
        private final Map.Entry<Transaction, Coins> date2coins1hash1 = entry(TRANSACTION_2, COINS_NEGATIVE);
        private final List<Map.Entry<Transaction, Coins>> entries =
                Lists.newArrayList(date1coins2hash2, date2coins1hash1);
        private final Set<TransactionSortOrder> sortByDate = Set.of(
                BY_DATE_THEN_HASH,
                BY_DATE_THEN_COINS_THEN_HASH,
                BY_DATE_THEN_COINS_ABSOLUTE_THEN_HASH,
                BY_COINS_ABSOLUTE_THEN_DATE_THEN_HASH
        );
        private final Set<TransactionSortOrder> sortByCoins = Set.of(BY_COINS_THEN_HASH, BY_COINS_THEN_DATE_THEN_HASH);

        @Test
        void sortsByHash() {
            for (TransactionSortOrder order : TransactionSortOrder.values()) {
                if (sortByDate.contains(order) || sortByCoins.contains(order)) {
                    continue;
                }
                assertOrder(entries, order, date2coins1hash1, date1coins2hash2);
            }
            softly.assertAll();
        }

        @Test
        void sortsByDate() {
            for (TransactionSortOrder order : sortByDate) {
                assertOrder(entries, order, date1coins2hash2, date2coins1hash1);
            }
            softly.assertAll();
        }

        @Test
        void sortsByCoins() {
            for (TransactionSortOrder order : sortByCoins) {
                assertOrder(entries, order, date2coins1hash1, date1coins2hash2);
            }
            softly.assertAll();
        }
    }

    @Nested
    class SameDateDifferentCoins {
        private final Map.Entry<Transaction, Coins> coins1hash3 = entry(TRANSACTION, COINS);
        private final Map.Entry<Transaction, Coins> coins2hash1 = entry(TRANSACTION_3, COINS.add(Coins.ofSatoshis(1)));
        private final Map.Entry<Transaction, Coins> coins3hash2 = entry(TRANSACTION_4, COINS.add(Coins.ofSatoshis(2)));
        private final List<Map.Entry<Transaction, Coins>> entries =
                Lists.newArrayList(coins1hash3, coins2hash1, coins3hash2);
        private final Set<TransactionSortOrder> sortByCoins = Set.of(
                BY_DATE_THEN_COINS_ABSOLUTE_THEN_HASH,
                BY_DATE_THEN_COINS_THEN_HASH,
                BY_COINS_THEN_HASH,
                BY_COINS_THEN_DATE_THEN_HASH,
                BY_COINS_ABSOLUTE_THEN_DATE_THEN_HASH,
                BY_COINS_ABSOLUTE_THEN_HASH
        );

        @Test
        void sortsByHash() {
            for (TransactionSortOrder order : TransactionSortOrder.values()) {
                if (sortByCoins.contains(order)) {
                    continue;
                }
                assertOrder(entries, order, coins2hash1, coins3hash2, coins1hash3);
            }
            softly.assertAll();
        }

        @Test
        void sortsByCoins() {
            for (TransactionSortOrder order : sortByCoins) {
                assertOrder(entries, order, coins1hash3, coins2hash1, coins3hash2);
            }
            softly.assertAll();
        }
    }

    @Nested
    class SameDateDifferentAbsoluteCoins {
        private final Map.Entry<Transaction, Coins> coins1hash2 = entry(TRANSACTION, COINS_NEGATIVE);
        private final Map.Entry<Transaction, Coins> coins2hash1 = entry(TRANSACTION_4, COINS);
        private final List<Map.Entry<Transaction, Coins>> entries = Lists.newArrayList(coins1hash2, coins2hash1);
        private final Set<TransactionSortOrder> sortByCoins =
                Set.of(BY_COINS_THEN_HASH, BY_DATE_THEN_COINS_THEN_HASH, BY_COINS_THEN_DATE_THEN_HASH);

        @Test
        void sortsByHash() {
            for (TransactionSortOrder order : TransactionSortOrder.values()) {
                if (sortByCoins.contains(order)) {
                    continue;
                }
                assertOrder(entries, order, coins2hash1, coins1hash2);
            }
            softly.assertAll();
        }

        @Test
        void sortsByCoins() {
            for (TransactionSortOrder order : sortByCoins) {
                assertOrder(entries, order, coins1hash2, coins2hash1);
            }
            softly.assertAll();
        }
    }

    private void assertOrder(
            List<Map.Entry<Transaction, Coins>> entriesToSort,
            TransactionSortOrder order,
            Map.Entry<Transaction, Coins>... expectedEntries
    ) {
        transactionSorter.setOrder(order);
        softly.assertThat(getSortedList(entriesToSort)).describedAs(order.toString()).containsExactly(expectedEntries);
    }

    private List<Map.Entry<Transaction, Coins>> getSortedList(List<Map.Entry<Transaction, Coins>> list) {
        list.sort(transactionSorter.getComparator());
        return list;
    }
}
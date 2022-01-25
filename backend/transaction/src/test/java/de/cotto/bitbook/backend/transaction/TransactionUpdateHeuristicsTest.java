package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Input;
import de.cotto.bitbook.backend.transaction.model.Output;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionUpdateHeuristicsTest {
    private static final int ONE_HOUR = 6;

    @SuppressWarnings("CPD-START")
    private static final int MANY_TRANSACTIONS_AGE_LIMIT = 4 * ONE_HOUR;
    private static final int RECENT_TRANSACTIONS_AGE_LIMIT = 6 * ONE_HOUR;
    private static final int WITH_BALANCE_AGE_LIMIT = 8 * ONE_HOUR;
    private static final int EMPTY_BALANCE_AGE_LIMIT = 24 * ONE_HOUR;
    private static final int LIMITED_USE_AGE_LIMIT = 7 * 24 * ONE_HOUR;
    @SuppressWarnings("CPD-END")

    private static final String ADDRESS = ADDRESS_TRANSACTIONS.getAddress();

    private static final Set<String> FEW_TRANSACTION_HASHES =
            Set.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
    private static final AddressTransactions FEW_TRANSACTIONS =
            new AddressTransactions(ADDRESS, FEW_TRANSACTION_HASHES, LAST_CHECKED_AT_BLOCK_HEIGHT);

    private static final Set<String> MANY_TRANSACTION_HASHES =
            Set.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
    private static final AddressTransactions MANY_TRANSACTIONS =
            new AddressTransactions(ADDRESS, MANY_TRANSACTION_HASHES, LAST_CHECKED_AT_BLOCK_HEIGHT);

    private static final AddressTransactions WITH_RECENT_TRANSACTIONS =
            new AddressTransactions(ADDRESS, Set.of("recent"), LAST_CHECKED_AT_BLOCK_HEIGHT);
    private static final AddressTransactions WITH_OLD_TRANSACTIONS =
            new AddressTransactions(ADDRESS, Set.of("old"), LAST_CHECKED_AT_BLOCK_HEIGHT);

    private static final AddressTransactions ONE_HASH =
            new AddressTransactions(ADDRESS, Set.of("foo"), LAST_CHECKED_AT_BLOCK_HEIGHT);
    private static final AddressTransactions TWO_HASHES = ADDRESS_TRANSACTIONS;

    private static final String SWEEP_TX_HASH = "sweep-tx";
    private static final AddressTransactions WITH_SWEEP_TRANSACTION =
            new AddressTransactions(ADDRESS, Set.of(SWEEP_TX_HASH), LAST_CHECKED_AT_BLOCK_HEIGHT);

    @InjectMocks
    private TransactionUpdateHeuristics transactionUpdateHeuristics;

    @Mock
    private BlockHeightService blockHeightService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionDao transactionDao;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Mock
    private TransactionDescriptionService transactionDescriptionService;

    @BeforeEach
    void setUp() {
        lenient().when(transactionDao.getTransaction(any())).thenReturn(Transaction.UNKNOWN);
        lenient().when(transactionDao.getTransaction("recent")).thenReturn(new Transaction(
                TRANSACTION.getHash(),
                TRANSACTION.getBlockHeight(),
                LocalDateTime.now(ZoneOffset.UTC).minusDays(6).minusHours(12),
                Coins.NONE,
                List.of(),
                List.of()
        ));
        lenient().when(transactionDao.getTransaction("old")).thenReturn(new Transaction(
                TRANSACTION.getHash(),
                TRANSACTION.getBlockHeight(),
                LocalDateTime.now(ZoneOffset.UTC).minusDays(7).minusHours(12),
                Coins.NONE,
                List.of(),
                List.of()
        ));
        lenient().when(addressDescriptionService.getDescription(any())).thenReturn("");
        lenient().when(transactionDescriptionService.getDescription(any())).thenReturn("");
    }

    @Test
    void age_limits_are_in_expected_order() {
        List<Integer> expectedOrder = List.of(
                MANY_TRANSACTIONS_AGE_LIMIT,
                RECENT_TRANSACTIONS_AGE_LIMIT,
                WITH_BALANCE_AGE_LIMIT,
                EMPTY_BALANCE_AGE_LIMIT,
                LIMITED_USE_AGE_LIMIT
        );
        ArrayList<Integer> sortedList = Lists.newArrayList(expectedOrder);
        sortedList.sort(Integer::compareTo);
        assertThat(sortedList).isEqualTo(expectedOrder);
    }

    @Test
    void recent_enough() {
        mockAge(WITH_BALANCE_AGE_LIMIT);
        mockBalance();
        assertThat(transactionUpdateHeuristics.isRecentEnough(ADDRESS_TRANSACTIONS)).isTrue();
    }

    @Test
    void too_old_with_balance() {
        mockAge(WITH_BALANCE_AGE_LIMIT + 1);
        mockBalance();
        assertThat(transactionUpdateHeuristics.isRecentEnough(ADDRESS_TRANSACTIONS)).isFalse();
    }

    @Test
    void recent_enough_no_balance() {
        mockAge(EMPTY_BALANCE_AGE_LIMIT);
        mockEmptyBalance();
        assertThat(transactionUpdateHeuristics.isRecentEnough(ADDRESS_TRANSACTIONS)).isTrue();
    }

    @Test
    void too_old_no_balance() {
        mockAge(EMPTY_BALANCE_AGE_LIMIT + 1);
        mockEmptyBalance();
        assertThat(transactionUpdateHeuristics.isRecentEnough(ADDRESS_TRANSACTIONS)).isFalse();
    }

    @Test
    void many_transactions_no_balance() {
        mockAge(MANY_TRANSACTIONS_AGE_LIMIT + 1);
        mockEmptyBalance();
        assertThat(transactionUpdateHeuristics.isRecentEnough(MANY_TRANSACTIONS)).isFalse();
    }

    @Test
    void many_transactions_too_old() {
        mockAge(MANY_TRANSACTIONS_AGE_LIMIT + 1);
        mockBalance();
        assertThat(transactionUpdateHeuristics.isRecentEnough(MANY_TRANSACTIONS)).isFalse();
    }

    @Test
    void many_transactions_recent() {
        mockAge(MANY_TRANSACTIONS_AGE_LIMIT);
        mockBalance();
        assertThat(transactionUpdateHeuristics.isRecentEnough(MANY_TRANSACTIONS)).isTrue();
    }

    @Test
    void few_transactions_recent() {
        mockAge(MANY_TRANSACTIONS_AGE_LIMIT + 1);
        mockBalance();
        assertThat(transactionUpdateHeuristics.isRecentEnough(FEW_TRANSACTIONS)).isTrue();
    }

    @Test
    void recent_transactions() {
        mockAge(RECENT_TRANSACTIONS_AGE_LIMIT);
        mockBalance();
        assertThat(transactionUpdateHeuristics.isRecentEnough(WITH_RECENT_TRANSACTIONS)).isTrue();
    }

    @Test
    void recent_transactions_too_old() {
        mockAge(RECENT_TRANSACTIONS_AGE_LIMIT + 1);
        mockBalance();
        assertThat(transactionUpdateHeuristics.isRecentEnough(WITH_RECENT_TRANSACTIONS)).isFalse();
    }

    @Test
    void old_transactions_recent() {
        mockAge(RECENT_TRANSACTIONS_AGE_LIMIT + 1);
        mockBalance();
        assertThat(transactionUpdateHeuristics.isRecentEnough(WITH_OLD_TRANSACTIONS)).isTrue();
    }

    @Test
    void single_use_input_of_sweep_transaction_recent() {
        mockAge(LIMITED_USE_AGE_LIMIT);
        mockBalance();
        mockSweepTransaction(ADDRESS, "output");
        assertThat(transactionUpdateHeuristics.isRecentEnough(WITH_SWEEP_TRANSACTION)).isTrue();
    }

    @Test
    void wrong_description_for_sweep_transaction() {
        mockAge(LIMITED_USE_AGE_LIMIT);
        mockBalance();
        mockSweepTransaction(ADDRESS, "output");
        when(transactionDescriptionService.getDescription(SWEEP_TX_HASH)).thenReturn("xlnd sweep transaction");
        assertThat(transactionUpdateHeuristics.isRecentEnough(WITH_SWEEP_TRANSACTION)).isFalse();
    }

    @Test
    void single_use_output_of_sweep_transaction_recent() {
        mockAge(LIMITED_USE_AGE_LIMIT);
        mockBalance();
        mockSweepTransaction("input", ADDRESS);
        assertThat(transactionUpdateHeuristics.isRecentEnough(WITH_SWEEP_TRANSACTION)).isFalse();
    }

    @Test
    void single_use_too_old() {
        mockAge(LIMITED_USE_AGE_LIMIT + 1);
        mockBalance();
        mockSweepTransaction(ADDRESS, "output");
        assertThat(transactionUpdateHeuristics.isRecentEnough(WITH_SWEEP_TRANSACTION)).isFalse();
    }

    @Test
    void dual_use_channel_opening_one_transaction_recent() {
        mockAge(LIMITED_USE_AGE_LIMIT);
        mockBalance();
        lenient().when(addressDescriptionService.getDescription(ADDRESS)).thenReturn("Lightning-Channel with foo");
        assertThat(transactionUpdateHeuristics.isRecentEnough(ONE_HASH)).isFalse();
    }

    @Test
    void dual_use_channel_opening_two_transactions_recent() {
        mockAge(LIMITED_USE_AGE_LIMIT);
        mockBalance();
        when(addressDescriptionService.getDescription(ADDRESS)).thenReturn("Lightning-Channel with foo");
        assertThat(transactionUpdateHeuristics.isRecentEnough(TWO_HASHES)).isTrue();
    }

    @Test
    void wrong_description_for_channel_opening_transaction() {
        mockAge(LIMITED_USE_AGE_LIMIT);
        mockBalance();
        when(addressDescriptionService.getDescription(ADDRESS)).thenReturn("xLightning-Channel with foo");
        assertThat(transactionUpdateHeuristics.isRecentEnough(TWO_HASHES)).isFalse();
    }

    @Test
    void dual_use_channel_opening_two_transactions_too_old() {
        mockAge(LIMITED_USE_AGE_LIMIT + 1);
        mockBalance();
        when(addressDescriptionService.getDescription(ADDRESS)).thenReturn("Lightning-Channel with foo");
        assertThat(transactionUpdateHeuristics.isRecentEnough(TWO_HASHES)).isFalse();
    }

    private void mockAge(int age) {
        when(blockHeightService.getBlockHeight(Chain.BTC)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT + age);
    }

    private void mockBalance() {
        List<Input> inputs = List.of(new Input(Coins.ofSatoshis(1), "xxx"));
        List<Output> outputs = List.of(new Output(Coins.ofSatoshis(1), ADDRESS));
        Transaction transaction = new Transaction(
                TRANSACTION.getHash(),
                TRANSACTION.getBlockHeight(),
                TRANSACTION.getTime(),
                Coins.NONE,
                inputs,
                outputs
        );
        lenient().when(transactionService.getTransactionDetails(ADDRESS_TRANSACTIONS.getTransactionHashes()))
                .thenReturn(Set.of(transaction));
    }

    private void mockEmptyBalance() {
        lenient().when(transactionService.getTransactionDetails(ADDRESS_TRANSACTIONS.getTransactionHashes()))
                .thenReturn(Set.of());
    }

    private void mockSweepTransaction(String inputAddress, String outputAddress) {
        when(transactionDescriptionService.getDescription(SWEEP_TX_HASH)).thenReturn("lnd sweep transaction");
        when(transactionDao.getTransaction(SWEEP_TX_HASH)).thenReturn(new Transaction(
                TRANSACTION.getHash(),
                TRANSACTION.getBlockHeight(),
                TRANSACTION.getTime(),
                Coins.NONE,
                List.of(new Input(Coins.ofSatoshis(1), inputAddress)),
                List.of(new Output(Coins.ofSatoshis(1), outputAddress))
        ));
    }
}
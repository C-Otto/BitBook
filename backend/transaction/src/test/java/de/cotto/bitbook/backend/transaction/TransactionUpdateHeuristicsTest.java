package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Input;
import de.cotto.bitbook.backend.model.Output;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
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
import java.util.stream.Stream;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.Chain.BTG;
import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.MEDIUM;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    private static final Address ADDRESS = ADDRESS_TRANSACTIONS.address();

    private static final Set<TransactionHash> FEW_TRANSACTION_HASHES =
            Stream.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
                    .map(TransactionHash::new)
                    .collect(toSet());
    private static final AddressTransactions FEW_TRANSACTIONS =
            new AddressTransactions(ADDRESS, FEW_TRANSACTION_HASHES, LAST_CHECKED_AT_BLOCK_HEIGHT, BTC);

    private static final Set<TransactionHash> MANY_TRANSACTION_HASHES =
            Stream.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11")
                    .map(TransactionHash::new)
                    .collect(toSet());
    private static final AddressTransactions MANY_TRANSACTIONS =
            new AddressTransactions(ADDRESS, MANY_TRANSACTION_HASHES, LAST_CHECKED_AT_BLOCK_HEIGHT, BTC);

    private static final AddressTransactions WITH_RECENT_TRANSACTIONS =
            new AddressTransactions(ADDRESS, Set.of(new TransactionHash("recent")), LAST_CHECKED_AT_BLOCK_HEIGHT, BTC);
    private static final AddressTransactions WITH_OLD_TRANSACTIONS =
            new AddressTransactions(ADDRESS, Set.of(new TransactionHash("old")), LAST_CHECKED_AT_BLOCK_HEIGHT, BTC);

    private static final AddressTransactions ONE_HASH =
            new AddressTransactions(ADDRESS, Set.of(new TransactionHash("foo")), LAST_CHECKED_AT_BLOCK_HEIGHT, BTC);
    private static final AddressTransactions TWO_HASHES = ADDRESS_TRANSACTIONS;

    private static final TransactionHash SWEEP_TX_HASH = new TransactionHash("sweep-tx");
    private static final AddressTransactions WITH_SWEEP_TRANSACTION =
            new AddressTransactions(ADDRESS, Set.of(SWEEP_TX_HASH), LAST_CHECKED_AT_BLOCK_HEIGHT, BTC);

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
        lenient().when(transactionDao.getTransaction(any(), eq(BTC))).thenReturn(Transaction.unknown(BTC));
        lenient().when(transactionDao.getTransaction(new TransactionHash("recent"), BTC)).thenReturn(new Transaction(
                TRANSACTION.getHash(),
                TRANSACTION.getBlockHeight(),
                LocalDateTime.now(ZoneOffset.UTC).minusDays(6).minusHours(12),
                Coins.NONE,
                List.of(),
                List.of(),
                BTC
        ));
        lenient().when(transactionDao.getTransaction(new TransactionHash("old"), BTC)).thenReturn(new Transaction(
                TRANSACTION.getHash(),
                TRANSACTION.getBlockHeight(),
                LocalDateTime.now(ZoneOffset.UTC).minusDays(7).minusHours(12),
                Coins.NONE,
                List.of(),
                List.of(),
                BTC
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
        mockSweepTransaction(ADDRESS, new Address("output"));
        assertThat(transactionUpdateHeuristics.isRecentEnough(WITH_SWEEP_TRANSACTION)).isTrue();
    }

    @Test
    void single_use_input_of_sweep_transaction_recent_but_wrong_chain() {
        when(blockHeightService.getBlockHeight(BTG)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT + LIMITED_USE_AGE_LIMIT);
        mockBalance(BTG);
        mockSweepTransaction(ADDRESS, new Address("output-btg"), BTG);
        AddressTransactions withSweepTransactionBtg =
                new AddressTransactions(ADDRESS, Set.of(SWEEP_TX_HASH), LAST_CHECKED_AT_BLOCK_HEIGHT, BTG);
        assertThat(transactionUpdateHeuristics.isRecentEnough(withSweepTransactionBtg)).isFalse();
    }

    @Test
    void wrong_description_for_sweep_transaction() {
        mockAge(LIMITED_USE_AGE_LIMIT);
        mockBalance();
        mockSweepTransaction(ADDRESS, new Address("output"));
        when(transactionDescriptionService.getDescription(SWEEP_TX_HASH)).thenReturn("xlnd sweep transaction");
        assertThat(transactionUpdateHeuristics.isRecentEnough(WITH_SWEEP_TRANSACTION)).isFalse();
    }

    @Test
    void single_use_output_of_sweep_transaction_recent() {
        mockAge(LIMITED_USE_AGE_LIMIT);
        mockBalance();
        mockSweepTransaction(new Address("input"), ADDRESS);
        assertThat(transactionUpdateHeuristics.isRecentEnough(WITH_SWEEP_TRANSACTION)).isFalse();
    }

    @Test
    void single_use_too_old() {
        mockAge(LIMITED_USE_AGE_LIMIT + 1);
        mockBalance();
        mockSweepTransaction(ADDRESS, new Address("output"));
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

    @Test
    void getRequestWithTweakedPriority_lowest_remains_lowest() {
        TransactionsRequestKey transactionsRequestKey = new TransactionsRequestKey(ADDRESS, BTC, BLOCK_HEIGHT);
        AddressTransactionsRequest request = AddressTransactionsRequest.create(transactionsRequestKey, LOWEST);
        assertThat(transactionUpdateHeuristics.getRequestWithTweakedPriority(request))
                .isEqualTo(request);
    }

    @Test
    void getRequestWithTweakedPriority_standard_is_downgraded_to_medium_priority() {
        TransactionsRequestKey transactionsRequestKey = new TransactionsRequestKey(ADDRESS, BTC, BLOCK_HEIGHT);
        AddressTransactionsRequest request = AddressTransactionsRequest.create(transactionsRequestKey, STANDARD);
        AddressTransactionsRequest requestMedium = AddressTransactionsRequest.create(transactionsRequestKey, MEDIUM);
        assertThat(transactionUpdateHeuristics.getRequestWithTweakedPriority(request))
                .isEqualTo(requestMedium);
    }

    private void mockAge(int age) {
        when(blockHeightService.getBlockHeight(BTC)).thenReturn(LAST_CHECKED_AT_BLOCK_HEIGHT + age);
    }

    private void mockBalance() {
        mockBalance(BTC);
    }

    private void mockBalance(Chain chain) {
        List<Input> inputs = List.of(new Input(Coins.ofSatoshis(1), new Address("xxx")));
        List<Output> outputs = List.of(new Output(Coins.ofSatoshis(1), ADDRESS));
        Transaction transaction = new Transaction(
                TRANSACTION.getHash(),
                TRANSACTION.getBlockHeight(),
                TRANSACTION.getTime(),
                Coins.NONE,
                inputs,
                outputs,
                chain
        );
        lenient().when(transactionService.getTransactionDetails(ADDRESS_TRANSACTIONS.transactionHashes(), chain))
                .thenReturn(Set.of(transaction));
    }

    private void mockEmptyBalance() {
        lenient().when(transactionService.getTransactionDetails(ADDRESS_TRANSACTIONS.transactionHashes(), BTC))
                .thenReturn(Set.of());
    }

    private void mockSweepTransaction(Address inputAddress, Address outputAddress) {
        mockSweepTransaction(inputAddress, outputAddress, BTC);
    }

    private void mockSweepTransaction(Address inputAddress, Address outputAddress, Chain chain) {
        lenient().when(transactionDescriptionService.getDescription(SWEEP_TX_HASH)).thenReturn("lnd sweep transaction");
        when(transactionDao.getTransaction(SWEEP_TX_HASH, chain)).thenReturn(new Transaction(
                TRANSACTION.getHash(),
                TRANSACTION.getBlockHeight(),
                TRANSACTION.getTime(),
                Coins.NONE,
                List.of(new Input(Coins.ofSatoshis(1), inputAddress)),
                List.of(new Output(Coins.ofSatoshis(1), outputAddress)),
                chain
        ));
    }
}
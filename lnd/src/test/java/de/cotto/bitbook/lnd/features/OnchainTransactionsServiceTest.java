package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import de.cotto.bitbook.lnd.model.OnchainTransaction;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_VALUE_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_VALUE_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.FUNDING_TRANSACTION;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.FUNDING_TRANSACTION_DETAILS;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.OPENING_TRANSACTION;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.OPENING_TRANSACTION_DETAILS;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_CLOSE;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_CLOSE_DETAILS;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_CLOSE_EXPIRY;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_CREATION;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_CREATION_DETAILS;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_DEPOSIT;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_DEPOSIT_DETAILS;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_DEPOSIT_WITH_FEES;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_ID;
import static de.cotto.bitbook.ownership.OwnershipStatus.OWNED;
import static de.cotto.bitbook.ownership.OwnershipStatus.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnchainTransactionsServiceTest {
    private static final String DEFAULT_DESCRIPTION = "lnd";

    @InjectMocks
    private OnchainTransactionsService onchainTransactionsService;

    @Mock
    private AddressOwnershipService addressOwnershipService;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Mock
    private TransactionDescriptionService transactionDescriptionService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private SweepTransactionsService sweepTransactionsService;

    @Test
    void loops_until_fixed_point_is_reached() {
        when(transactionService.getTransactionDetails(FUNDING_TRANSACTION.getTransactionHash()))
                .thenReturn(FUNDING_TRANSACTION_DETAILS);
        onchainTransactionsService.addFromOnchainTransactions(Set.of(FUNDING_TRANSACTION));
        verify(addressOwnershipService, times(2)).setAddressAsOwned(OUTPUT_ADDRESS_2);
    }

    @Nested
    class FundingTransactionSuccess {
        @Test
        void sets_ownership_for_funding_transaction() {
            when(transactionService.getTransactionDetails(FUNDING_TRANSACTION.getTransactionHash()))
                    .thenReturn(FUNDING_TRANSACTION_DETAILS);
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(FUNDING_TRANSACTION))).isEqualTo(1);
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(OUTPUT_ADDRESS_2);
        }

        @Test
        void sets_description_for_funding_transaction() {
            when(transactionService.getTransactionDetails(FUNDING_TRANSACTION.getTransactionHash()))
                    .thenReturn(FUNDING_TRANSACTION_DETAILS);
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(FUNDING_TRANSACTION))).isEqualTo(1);
            verify(addressDescriptionService, atLeastOnce()).set(OUTPUT_ADDRESS_2, DEFAULT_DESCRIPTION);
        }
    }

    @Nested
    class FundingTransactionFailure {
        @Test
        void nothing_for_funding_transaction_but_with_label() {
            OnchainTransaction onchainTransaction = new OnchainTransaction(
                    TRANSACTION_HASH,
                    "foo",
                    OUTPUT_VALUE_2,
                    Coins.NONE
            );
            assertFailure(onchainTransaction);
        }

        @Test
        void nothing_for_funding_transaction_but_with_fees() {
            OnchainTransaction onchainTransaction = new OnchainTransaction(
                    TRANSACTION_HASH,
                    "",
                    OUTPUT_VALUE_2,
                    Coins.ofSatoshis(1)
            );
            assertFailure(onchainTransaction);
        }

        @Test
        void nothing_for_funding_transaction_but_with_negative_amount() {
            OnchainTransaction onchainTransaction = new OnchainTransaction(
                    TRANSACTION_HASH,
                    "",
                    Coins.ofSatoshis(-1),
                    Coins.NONE
            );
            assertFailure(onchainTransaction);
        }

        @Test
        void nothing_for_funding_transaction_but_no_matching_output() {
            OnchainTransaction onchainTransaction = new OnchainTransaction(
                    TRANSACTION_HASH,
                    "",
                    OUTPUT_VALUE_2.add(Coins.ofSatoshis(1)),
                    Coins.NONE
            );
            when(transactionService.getTransactionDetails(anyString())).thenReturn(TRANSACTION);
            assertFailure(onchainTransaction);
        }

        private void assertFailure(OnchainTransaction onchainTransaction) {
            lenient().when(transactionService.getTransactionDetails(anyString()))
                    .thenReturn(Transaction.UNKNOWN);
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(onchainTransaction))).isEqualTo(0);
            verify(addressDescriptionService, never()).set(eq(OUTPUT_ADDRESS_2), any());
        }
    }

    @Nested
    class OpeningTransactionSuccess {
        @BeforeEach
        void setUp() {
            when(transactionService.getTransactionDetails(OPENING_TRANSACTION.getTransactionHash()))
                    .thenReturn(OPENING_TRANSACTION_DETAILS);
            String unknownOutputAddress = OUTPUT_ADDRESS_1;

            when(addressOwnershipService.getOwnershipStatus(unknownOutputAddress)).thenReturn(UNKNOWN);
            when(addressDescriptionService.getDescription(unknownOutputAddress)).thenReturn("");

            setupInputMocksForOpeningTransaction();

            when(addressDescriptionService.getDescription(OUTPUT_ADDRESS_2))
                    .thenReturn(ChannelsService.ADDRESS_DESCRIPTION_PREFIX + "foo");
        }

        @Test
        void sets_ownership_for_change_output_of_channel_opening_transaction() {
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(OPENING_TRANSACTION))).isEqualTo(1);
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(OUTPUT_ADDRESS_1);
        }

        @Test
        void sets_ownership_for_channel_output_of_channel_opening_transaction() {
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(OPENING_TRANSACTION))).isEqualTo(1);
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(OUTPUT_ADDRESS_2);
        }

        @Test
        void sets_description_for_change_output_of_channel_opening_transaction() {
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(OPENING_TRANSACTION))).isEqualTo(1);
            verify(addressDescriptionService, atLeastOnce()).set(OUTPUT_ADDRESS_1, DEFAULT_DESCRIPTION);
        }

        @Test
        void does_not_set_description_for_channel_output_of_channel_opening_transaction() {
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(OPENING_TRANSACTION))).isEqualTo(1);
            verify(addressDescriptionService, never()).set(OUTPUT_ADDRESS_2, DEFAULT_DESCRIPTION);
        }
    }

    @Nested
    class OpeningTransactionFailure {
        @BeforeEach
        void setUp() {
            lenient().when(transactionService.getTransactionDetails(OPENING_TRANSACTION.getTransactionHash()))
                    .thenReturn(OPENING_TRANSACTION_DETAILS);
        }

        @Test
        void transaction_with_zero_amount() {
            OnchainTransaction openingTransaction = new OnchainTransaction(
                    TRANSACTION_HASH,
                    "",
                    Coins.NONE,
                    Coins.NONE
            );
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(openingTransaction))).isEqualTo(0);
            verifyNoInteractions(addressOwnershipService);
        }

        @Test
        void transaction_with_label() {
            OnchainTransaction openingTransaction = new OnchainTransaction(
                    TRANSACTION_HASH,
                    "xxx",
                    Coins.ofSatoshis(-1_234 - 21_513),
                    Coins.ofSatoshis(21_513)
            );
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(openingTransaction))).isEqualTo(0);
            verifyNoInteractions(addressOwnershipService);
        }

        @Test
        void transaction_with_unowned_input() {
            when(addressOwnershipService.getOwnershipStatus(INPUT_ADDRESS_1)).thenReturn(OWNED);
            when(addressOwnershipService.getOwnershipStatus(INPUT_ADDRESS_2)).thenReturn(UNKNOWN);
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(OPENING_TRANSACTION))).isEqualTo(0);
            verify(addressOwnershipService, never()).setAddressAsOwned(any());
        }

        @Test
        void transaction_with_unexpected_description_for_input() {
            setupInputMocksForOpeningTransaction();
            when(addressDescriptionService.getDescription(INPUT_ADDRESS_2)).thenReturn("lnd?");
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(OPENING_TRANSACTION))).isEqualTo(0);
            verify(addressOwnershipService, never()).setAddressAsOwned(any());
        }

        @Test
        void transaction_with_unexpected_description_for_channel_output() {
            setupInputMocksForOpeningTransaction();
            when(addressDescriptionService.getDescription(OUTPUT_ADDRESS_1)).thenReturn("");
            when(addressDescriptionService.getDescription(OUTPUT_ADDRESS_2)).thenReturn("Lightning-Channel with_xxx");
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(OPENING_TRANSACTION))).isEqualTo(0);
            verify(addressOwnershipService, never()).setAddressAsOwned(any());
        }

        @Test
        void transaction_with_unexpected_channel_capacity() {
            setupInputMocksForOpeningTransaction();
            OnchainTransaction openingTransaction = new OnchainTransaction(
                    TRANSACTION_HASH,
                    "",
                    Coins.ofSatoshis(-1_234 - 21_513 - 1),
                    Coins.ofSatoshis(21_513)
            );
            when(addressDescriptionService.getDescription(OUTPUT_ADDRESS_1)).thenReturn("");
            when(addressDescriptionService.getDescription(OUTPUT_ADDRESS_2)).thenReturn("Lightning-Channel with xxx");
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(openingTransaction))).isEqualTo(0);
            verify(addressOwnershipService, never()).setAddressAsOwned(any());
        }
    }

    @Nested
    class PoolAccountCreationSuccess {
        @BeforeEach
        void setUp() {
            when(transactionService.getTransactionDetails(POOL_ACCOUNT_CREATION.getTransactionHash()))
                    .thenReturn(POOL_ACCOUNT_CREATION_DETAILS);
        }

        @Test
        void returns_number_of_accepted_transactions() {
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_CREATION)))
                    .isEqualTo(1);
        }

        @Test
        void sets_transaction_description() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_CREATION));
            verify(transactionDescriptionService, atLeastOnce()).set(
                    POOL_ACCOUNT_CREATION_DETAILS.getHash(),
                    "Creating pool account " + POOL_ACCOUNT_ID
            );
        }

        @Test
        void sets_description_for_pool_address() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_CREATION));
            verify(addressDescriptionService, atLeastOnce()).set(OUTPUT_ADDRESS_2, "pool account " + POOL_ACCOUNT_ID);
        }

        @Test
        void sets_ownership_for_pool_address() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_CREATION));
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(OUTPUT_ADDRESS_2);
        }

        @Test
        void sets_description_for_other_outputs() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_CREATION));
            verify(addressDescriptionService, atLeastOnce()).set(OUTPUT_ADDRESS_1, DEFAULT_DESCRIPTION);
        }

        @Test
        void sets_ownership_for_other_outputs() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_CREATION));
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(OUTPUT_ADDRESS_1);
        }

        @Test
        void sets_ownership_for_inputs() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_CREATION));
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(INPUT_ADDRESS_1);
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(INPUT_ADDRESS_2);
        }

        @Test
        void sets_descriptions_for_inputs() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_CREATION));
            verify(addressDescriptionService, atLeastOnce()).set(INPUT_ADDRESS_1, DEFAULT_DESCRIPTION);
            verify(addressDescriptionService, atLeastOnce()).set(INPUT_ADDRESS_2, DEFAULT_DESCRIPTION);
        }
    }

    @Nested
    class PoolAccountCreationFailure {
        @Test
        void positive_amount() {
            OnchainTransaction transaction = new OnchainTransaction(
                        TRANSACTION_HASH,
                        " poold -- AccountCreation(acct_key=" + POOL_ACCOUNT_ID + ")",
                        Coins.ofSatoshis(123),
                        Coins.NONE
                );
            assertFailure(transaction);
        }

        @Test
        void wrong_label() {
            OnchainTransaction transaction = new OnchainTransaction(
                    TRANSACTION_HASH,
                    "poold -- AccountCreation(acct_key=" + POOL_ACCOUNT_ID + ")", // leading space missing
                    Coins.ofSatoshis(-1_234 - 999),
                    Coins.ofSatoshis(999)
            );
            assertFailure(transaction);
        }

        @Test
        void mismatching_amount_for_pool_address() {
            when(transactionService.getTransactionDetails(POOL_ACCOUNT_CREATION.getTransactionHash()))
                    .thenReturn(POOL_ACCOUNT_CREATION_DETAILS);
            OnchainTransaction transaction = new OnchainTransaction(
                    POOL_ACCOUNT_CREATION.getTransactionHash(),
                    POOL_ACCOUNT_CREATION.getLabel(),
                    POOL_ACCOUNT_CREATION.getAmount().subtract(Coins.ofSatoshis(1)),
                    POOL_ACCOUNT_CREATION.getFees()
            );
            assertFailure(transaction);
        }
    }

    @Nested
    class PoolAccountCloseSuccess {
        @BeforeEach
        void setUp() {
            when(transactionService.getTransactionDetails(POOL_ACCOUNT_CLOSE.getTransactionHash()))
                    .thenReturn(POOL_ACCOUNT_CLOSE_DETAILS);
        }

        @Test
        void returns_number_of_accepted_transactions() {
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_CLOSE)))
                    .isEqualTo(1);
        }

        @Test
        void account_expiry() {
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_CLOSE_EXPIRY)))
                    .isEqualTo(1);
        }

        @Test
        void sets_transaction_description() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_CLOSE));
            verify(transactionDescriptionService, atLeastOnce()).set(
                    POOL_ACCOUNT_CLOSE_DETAILS.getHash(),
                    "Closing pool account " + POOL_ACCOUNT_ID
            );
        }

        @Test
        void sets_description_for_pool_addresses() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_CLOSE));
            verify(addressDescriptionService, atLeastOnce()).set(INPUT_ADDRESS_1, "pool account " + POOL_ACCOUNT_ID);
        }

        @Test
        void sets_ownership_for_pool_addresses() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_CLOSE));
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(INPUT_ADDRESS_1);
        }

        @Test
        void sets_description_for_outputs() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_CLOSE));
            verify(addressDescriptionService, atLeastOnce()).set(OUTPUT_ADDRESS_1, DEFAULT_DESCRIPTION);
        }

        @Test
        void sets_ownership_for_outputs() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_CLOSE));
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(OUTPUT_ADDRESS_1);
        }
    }

    @Nested
    class PoolAccountCloseFailure {
        @Test
        void negative_amount() {
            OnchainTransaction transaction = new OnchainTransaction(
                    POOL_ACCOUNT_CLOSE.getTransactionHash(),
                    POOL_ACCOUNT_CLOSE.getLabel(),
                    Coins.ofSatoshis(-123),
                    Coins.NONE
            );
            assertFailure(transaction);
        }

        @Test
        void wrong_label() {
            String label = "poold -- AccountModification(acct_key=" // leading space missing
                           + POOL_ACCOUNT_ID
                           + ", expiry=false, deposit=false, is_close=true)";
            OnchainTransaction transaction = new OnchainTransaction(
                    POOL_ACCOUNT_CLOSE.getTransactionHash(),
                    label,
                    POOL_ACCOUNT_CLOSE.getAmount(),
                    POOL_ACCOUNT_CLOSE.getFees()
            );
            assertFailure(transaction);
        }

        @Test
        void mismatching_amount_for_pool_address() {
            when(transactionService.getTransactionDetails(POOL_ACCOUNT_CLOSE.getTransactionHash()))
                    .thenReturn(POOL_ACCOUNT_CLOSE_DETAILS);
            OnchainTransaction transaction = new OnchainTransaction(
                    POOL_ACCOUNT_CLOSE.getTransactionHash(),
                    POOL_ACCOUNT_CLOSE.getLabel(),
                    POOL_ACCOUNT_CLOSE.getAmount().add(Coins.ofSatoshis(1)),
                    POOL_ACCOUNT_CLOSE.getFees()
            );
            assertFailure(transaction);
        }

        @Test
        void two_outputs() {
            when(transactionService.getTransactionDetails(POOL_ACCOUNT_CLOSE.getTransactionHash()))
                    .thenReturn(TRANSACTION);
            OnchainTransaction transaction = new OnchainTransaction(
                    POOL_ACCOUNT_CLOSE.getTransactionHash(),
                    POOL_ACCOUNT_CLOSE.getLabel(),
                    OUTPUT_VALUE_1.add(OUTPUT_VALUE_2),
                    POOL_ACCOUNT_CLOSE.getFees()
            );
            assertFailure(transaction);
        }
    }

    @Nested
    class Sweep {
        @Test
        void forwards_to_sweep_transaction_service() {
            when(transactionService.getTransactionDetails(anyString())).thenReturn(Transaction.UNKNOWN);
            when(sweepTransactionsService.addFromSweeps(Set.of(TRANSACTION_HASH))).thenReturn(123L);
            OnchainTransaction transaction = new OnchainTransaction(
                    TRANSACTION_HASH,
                    "",
                    Coins.ofSatoshis(-123),
                    Coins.ofSatoshis(123)
            );
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(transaction))).isEqualTo(123);
        }

        @Test
        void amount_does_not_match_fee() {
            when(transactionService.getTransactionDetails(anyString())).thenReturn(Transaction.UNKNOWN);
            OnchainTransaction transaction = new OnchainTransaction(
                    TRANSACTION_HASH,
                    "",
                    Coins.ofSatoshis(-101),
                    Coins.ofSatoshis(100)
            );
            assertFailure(transaction);
            verifyNoInteractions(sweepTransactionsService);
        }

        @Test
        void amount_zero() {
            when(transactionService.getTransactionDetails(anyString())).thenReturn(Transaction.UNKNOWN);
            OnchainTransaction transaction = new OnchainTransaction(
                    TRANSACTION_HASH,
                    "",
                    Coins.NONE,
                    Coins.NONE
            );
            assertFailure(transaction);
            verifyNoInteractions(sweepTransactionsService);
        }

        @Test
        void has_label() {
            OnchainTransaction transaction = new OnchainTransaction(
                    TRANSACTION_HASH,
                    "hello",
                    Coins.ofSatoshis(-123),
                    Coins.ofSatoshis(123)
            );
            assertFailure(transaction);
            verifyNoInteractions(sweepTransactionsService);
        }
    }

    @Nested
    class PoolAccountDepositSuccess {
        @BeforeEach
        void setUp() {
            when(addressDescriptionService.getDescription(INPUT_ADDRESS_1)).thenReturn("");
            when(addressDescriptionService.getDescription(INPUT_ADDRESS_2)).thenReturn(DEFAULT_DESCRIPTION);
            when(transactionService.getTransactionDetails(POOL_ACCOUNT_DEPOSIT.getTransactionHash()))
                    .thenReturn(POOL_ACCOUNT_DEPOSIT_DETAILS);
        }

        @Test
        void returns_number_of_accepted_transactions() {
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_DEPOSIT)))
                    .isEqualTo(1);
        }

        @Test
        void with_fees() {
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_DEPOSIT_WITH_FEES)))
                    .isEqualTo(1);
        }

        @Test
        void sets_transaction_description() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_DEPOSIT));
            verify(transactionDescriptionService, atLeastOnce()).set(
                    POOL_ACCOUNT_DEPOSIT_DETAILS.getHash(),
                    "Deposit into pool account " + POOL_ACCOUNT_ID
            );
        }

        @Test
        void sets_description_for_pool_addresses() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_DEPOSIT));
            verify(addressDescriptionService, atLeastOnce()).set(OUTPUT_ADDRESS_1, "pool account " + POOL_ACCOUNT_ID);
        }

        @Test
        void sets_ownership_for_pool_addresses() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_DEPOSIT));
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(OUTPUT_ADDRESS_2);
        }

        @Test
        void sets_description_for_other_output() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_DEPOSIT));
            verify(addressDescriptionService, atLeastOnce()).set(OUTPUT_ADDRESS_2, DEFAULT_DESCRIPTION);
        }

        @Test
        void sets_ownership_for_other_outputs() {
            onchainTransactionsService.addFromOnchainTransactions(Set.of(POOL_ACCOUNT_DEPOSIT));
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(OUTPUT_ADDRESS_1);
        }
    }

    @Nested
    class PoolAccountDepositFailure {
        @Test
        void nonnegative_amount() {
            OnchainTransaction transaction = new OnchainTransaction(
                    POOL_ACCOUNT_DEPOSIT.getTransactionHash(),
                    POOL_ACCOUNT_DEPOSIT.getLabel(),
                    Coins.ofSatoshis(123),
                    Coins.NONE
            );
            assertFailure(transaction);
        }

        @Test
        void wrong_label() {
            String label = "poold -- AccountModification(acct_key=" // leading space missing
                           + POOL_ACCOUNT_ID
                           + ", expiry=false, deposit=true, is_close=false)";
            OnchainTransaction transaction = new OnchainTransaction(
                    POOL_ACCOUNT_DEPOSIT.getTransactionHash(),
                    label,
                    POOL_ACCOUNT_DEPOSIT.getAmount(),
                    POOL_ACCOUNT_DEPOSIT.getFees()
            );
            assertFailure(transaction);
        }

        @Test
        void mismatching_amount_for_pool_address() {
            when(transactionService.getTransactionDetails(POOL_ACCOUNT_DEPOSIT.getTransactionHash()))
                    .thenReturn(POOL_ACCOUNT_DEPOSIT_DETAILS);
            OnchainTransaction transaction = new OnchainTransaction(
                    POOL_ACCOUNT_DEPOSIT.getTransactionHash(),
                    POOL_ACCOUNT_DEPOSIT.getLabel(),
                    POOL_ACCOUNT_DEPOSIT.getAmount().add(Coins.ofSatoshis(1)),
                    POOL_ACCOUNT_DEPOSIT.getFees()
            );
            assertFailure(transaction);
        }
    }

    private void setupInputMocksForOpeningTransaction() {
        when(addressOwnershipService.getOwnershipStatus(INPUT_ADDRESS_1)).thenReturn(OWNED);
        when(addressOwnershipService.getOwnershipStatus(INPUT_ADDRESS_2)).thenReturn(OWNED);
        when(addressDescriptionService.getDescription(INPUT_ADDRESS_1)).thenReturn(DEFAULT_DESCRIPTION);
        when(addressDescriptionService.getDescription(INPUT_ADDRESS_2)).thenReturn(DEFAULT_DESCRIPTION);
    }

    private void assertFailure(OnchainTransaction transaction) {
        assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(transaction))).isEqualTo(0);
        verify(addressDescriptionService, never()).set(any(), any());
    }
}
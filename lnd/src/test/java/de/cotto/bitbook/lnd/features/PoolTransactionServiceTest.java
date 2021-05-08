package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.lnd.model.OnchainTransaction;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_VALUE_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_VALUE_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_CLOSE;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_CLOSE_DETAILS;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_CLOSE_EXPIRY;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_CREATION;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_CREATION_DETAILS;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_DEPOSIT;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_DEPOSIT_DETAILS;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_DEPOSIT_WITH_FEES;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.POOL_ACCOUNT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PoolTransactionServiceTest {
    private static final String DEFAULT_DESCRIPTION = "lnd";

    @InjectMocks
    private PoolTransactionService poolTransactionService;
    
    @Mock
    private TransactionService transactionService;

    @Mock
    private AddressOwnershipService addressOwnershipService;

    @Mock
    private TransactionDescriptionService transactionDescriptionService;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Nested
    class PoolAccountCreationSuccess {
        private static final String PREFIX = "pool account ";

        @BeforeEach
        void setUp() {
            when(transactionService.getTransactionDetails(POOL_ACCOUNT_CREATION.getTransactionHash()))
                    .thenReturn(POOL_ACCOUNT_CREATION_DETAILS);
        }

        @Test
        void returns_number_of_accepted_transactions() {
            assertThat(poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_CREATION))
                    .isEqualTo(1);
        }

        @Test
        void sets_transaction_description() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_CREATION);
            verify(transactionDescriptionService, atLeastOnce()).set(
                    POOL_ACCOUNT_CREATION_DETAILS.getHash(),
                    "Creating pool account " + POOL_ACCOUNT_ID
            );
        }

        @Test
        void sets_description_for_pool_address() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_CREATION);
            verify(addressDescriptionService, atLeastOnce()).set(OUTPUT_ADDRESS_2, PREFIX + POOL_ACCOUNT_ID);
            verify(addressDescriptionService, never()).set(OUTPUT_ADDRESS_1, PREFIX + POOL_ACCOUNT_ID);
        }

        @Test
        void sets_ownership_for_pool_address() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_CREATION);
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(OUTPUT_ADDRESS_2);
        }

        @Test
        void sets_description_for_other_outputs() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_CREATION);
            verify(addressDescriptionService, atLeastOnce()).set(OUTPUT_ADDRESS_1, DEFAULT_DESCRIPTION);
            verify(addressDescriptionService, never()).set(OUTPUT_ADDRESS_2, DEFAULT_DESCRIPTION);
        }

        @Test
        void sets_ownership_for_other_outputs() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_CREATION);
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(OUTPUT_ADDRESS_1);
        }

        @Test
        void sets_ownership_for_inputs() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_CREATION);
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(INPUT_ADDRESS_1);
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(INPUT_ADDRESS_2);
        }

        @Test
        void sets_descriptions_for_inputs() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_CREATION);
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
        private static final String PREFIX = "pool account ";

        @BeforeEach
        void setUp() {
            when(transactionService.getTransactionDetails(POOL_ACCOUNT_CLOSE.getTransactionHash()))
                    .thenReturn(POOL_ACCOUNT_CLOSE_DETAILS);
        }

        @Test
        void returns_number_of_accepted_transactions() {
            assertThat(poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_CLOSE))
                    .isEqualTo(1);
        }

        @Test
        void account_expiry() {
            assertThat(poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_CLOSE_EXPIRY))
                    .isEqualTo(1);
        }

        @Test
        void sets_transaction_description() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_CLOSE);
            verify(transactionDescriptionService, atLeastOnce()).set(
                    POOL_ACCOUNT_CLOSE_DETAILS.getHash(),
                    "Closing pool account " + POOL_ACCOUNT_ID
            );
        }

        @Test
        void sets_description_for_pool_addresses() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_CLOSE);
            verify(addressDescriptionService, atLeastOnce()).set(INPUT_ADDRESS_1, PREFIX + POOL_ACCOUNT_ID);
        }

        @Test
        void sets_ownership_for_pool_addresses() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_CLOSE);
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(INPUT_ADDRESS_1);
        }

        @Test
        void sets_description_for_outputs() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_CLOSE);
            verify(addressDescriptionService, atLeastOnce()).set(OUTPUT_ADDRESS_1, DEFAULT_DESCRIPTION);
        }

        @Test
        void sets_ownership_for_outputs() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_CLOSE);
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
    class PoolAccountDepositSuccess {
        private static final String PREFIX = "pool account ";

        @BeforeEach
        void setUp() {
            when(addressDescriptionService.getDescription(INPUT_ADDRESS_1)).thenReturn("");
            when(addressDescriptionService.getDescription(INPUT_ADDRESS_2)).thenReturn(DEFAULT_DESCRIPTION);
            when(transactionService.getTransactionDetails(POOL_ACCOUNT_DEPOSIT.getTransactionHash()))
                    .thenReturn(POOL_ACCOUNT_DEPOSIT_DETAILS);
        }

        @Test
        void returns_number_of_accepted_transactions() {
            assertThat(poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_DEPOSIT))
                    .isEqualTo(1);
        }

        @Test
        void with_fees() {
            assertThat(poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_DEPOSIT_WITH_FEES))
                    .isEqualTo(1);
        }

        @Test
        void sets_transaction_description() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_DEPOSIT);
            verify(transactionDescriptionService, atLeastOnce()).set(
                    POOL_ACCOUNT_DEPOSIT_DETAILS.getHash(),
                    "Deposit into pool account " + POOL_ACCOUNT_ID
            );
        }

        @Test
        void sets_description_for_pool_addresses() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_DEPOSIT);
            verify(addressDescriptionService, atLeastOnce()).set(OUTPUT_ADDRESS_1, PREFIX + POOL_ACCOUNT_ID);
            verify(addressDescriptionService, never()).set(OUTPUT_ADDRESS_2, PREFIX + POOL_ACCOUNT_ID);
        }

        @Test
        void sets_ownership_for_pool_addresses() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_DEPOSIT);
            verify(addressOwnershipService, atLeastOnce()).setAddressAsOwned(OUTPUT_ADDRESS_2);
        }

        @Test
        void sets_description_for_other_output() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_DEPOSIT);
            verify(addressDescriptionService, atLeastOnce()).set(OUTPUT_ADDRESS_2, DEFAULT_DESCRIPTION);
            verify(addressDescriptionService, never()).set(OUTPUT_ADDRESS_1, DEFAULT_DESCRIPTION);
        }

        @Test
        void sets_ownership_for_other_outputs() {
            poolTransactionService.addFromOnchainTransaction(POOL_ACCOUNT_DEPOSIT);
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

    private void assertFailure(OnchainTransaction transaction) {
        assertThat(poolTransactionService.addFromOnchainTransaction(transaction)).isEqualTo(0);
        verify(addressDescriptionService, never()).set(any(), any());
    }
}
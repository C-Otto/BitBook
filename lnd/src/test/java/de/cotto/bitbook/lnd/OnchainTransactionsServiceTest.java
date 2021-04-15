package de.cotto.bitbook.lnd;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.lnd.model.OnchainTransaction;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_VALUE_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.FUNDING_TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnchainTransactionsServiceTest {
    @InjectMocks
    private OnchainTransactionsService onchainTransactionsService;

    @Mock
    private AddressOwnershipService addressOwnershipService;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Mock
    private TransactionService transactionService;

    @Test
    void sets_ownership_for_funding_transaction() {
        when(transactionService.getTransactionDetails(FUNDING_TRANSACTION.getTransactionHash()))
                .thenReturn(TRANSACTION);
        assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(FUNDING_TRANSACTION))).isEqualTo(1);
        verify(addressOwnershipService).setAddressAsOwned(OUTPUT_ADDRESS_2);
    }

    @Test
    void sets_description_for_funding_transaction() {
        when(transactionService.getTransactionDetails(FUNDING_TRANSACTION.getTransactionHash()))
                .thenReturn(TRANSACTION);
        assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(FUNDING_TRANSACTION))).isEqualTo(1);
        verify(addressDescriptionService).set(OUTPUT_ADDRESS_2, "lnd");
    }

    @Nested
    class Failure {
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
            assertThat(onchainTransactionsService.addFromOnchainTransactions(Set.of(onchainTransaction))).isEqualTo(0);
            verify(addressDescriptionService, never()).set(eq(OUTPUT_ADDRESS_2), any());
        }
    }
}
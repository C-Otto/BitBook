package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Input;
import de.cotto.bitbook.backend.transaction.model.Output;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_4;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SweepTransactionsServiceTest {
    private static final String DEFAULT_DESCRIPTION = "lnd";

    @InjectMocks
    private SweepTransactionsService sweepTransactionsService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AddressOwnershipService addressOwnershipService;

    @Mock
    private TransactionDescriptionService transactionDescriptionService;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Test
    void unknown_transaction() {
        when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH)))
                .thenReturn(Set.of(Transaction.UNKNOWN));
        assertFailure(Set.of(TRANSACTION_HASH));
    }

    @Test
    void not_sweep_because_of_two_outputs() {
        when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH))).thenReturn(Set.of(TRANSACTION_2));
        assertFailure(Set.of(TRANSACTION_HASH));
    }

    @Nested
    class Success {
        private static final Input INPUT_SWEEP_1 = new Input(Coins.ofSatoshis(200), "input-address1");
        private static final Input INPUT_SWEEP_2 = new Input(Coins.ofSatoshis(2), "input-address2");
        private static final Output OUTPUT_SWEEP_1 = new Output(Coins.ofSatoshis(199), "output-address1");
        private static final Output OUTPUT_SWEEP_2 = new Output(Coins.ofSatoshis(2), "output-address2");

        private static final Transaction SWEEP_TRANSACTION = new Transaction(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                Coins.ofSatoshis(1),
                List.of(INPUT_SWEEP_1),
                List.of(OUTPUT_SWEEP_1)
        );

        private static final Transaction SWEEP_TRANSACTION_2 = new Transaction(
                TRANSACTION_HASH_2,
                BLOCK_HEIGHT,
                DATE_TIME,
                Coins.ofSatoshis(0),
                List.of(INPUT_SWEEP_2),
                List.of(OUTPUT_SWEEP_2)
        );

        private final Set<String> hashes = Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2);

        @BeforeEach
        void setUp() {
            when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2)))
                    .thenReturn(Set.of(SWEEP_TRANSACTION, SWEEP_TRANSACTION_2));
        }

        @Test
        void returns_number_of_hashes() {
            assertThat(sweepTransactionsService.addFromSweeps(hashes)).isEqualTo(2);
        }

        @Test
        void accepts_two_inputs() {
            // this may happen for closed channels with unsettled HTLCs
            when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2)))
                    .thenReturn(Set.of(TRANSACTION_4, SWEEP_TRANSACTION_2));
            assertThat(sweepTransactionsService.addFromSweeps(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2)))
                    .isEqualTo(2);
        }

        @Test
        void sets_transaction_descriptions() {
            String expectedDescription = "lnd sweep transaction";

            sweepTransactionsService.addFromSweeps(hashes);

            verify(transactionDescriptionService).set(TRANSACTION_HASH, expectedDescription);
            verify(transactionDescriptionService).set(TRANSACTION_HASH_2, expectedDescription);
        }

        @Test
        void sets_address_descriptions() {
            sweepTransactionsService.addFromSweeps(hashes);

            verify(addressDescriptionService).set(INPUT_SWEEP_1.getAddress(), DEFAULT_DESCRIPTION);
            verify(addressDescriptionService).set(INPUT_SWEEP_2.getAddress(), DEFAULT_DESCRIPTION);
            verify(addressDescriptionService).set(OUTPUT_SWEEP_1.getAddress(), DEFAULT_DESCRIPTION);
            verify(addressDescriptionService).set(OUTPUT_SWEEP_2.getAddress(), DEFAULT_DESCRIPTION);
        }

        @Test
        void sets_address_ownership() {
            sweepTransactionsService.addFromSweeps(hashes);

            verify(addressOwnershipService).setAddressAsOwned(INPUT_SWEEP_1.getAddress());
            verify(addressOwnershipService).setAddressAsOwned(INPUT_SWEEP_2.getAddress());
            verify(addressOwnershipService).setAddressAsOwned(OUTPUT_SWEEP_1.getAddress());
            verify(addressOwnershipService).setAddressAsOwned(OUTPUT_SWEEP_2.getAddress());
        }
    }

    private void assertFailure(Set<String> hashes) {
        assertThat(sweepTransactionsService.addFromSweeps(hashes)).isEqualTo(0);
        verifyNoInteractions(addressOwnershipService);
    }
}

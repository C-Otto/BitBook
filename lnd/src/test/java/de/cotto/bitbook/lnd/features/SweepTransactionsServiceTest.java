package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Input;
import de.cotto.bitbook.backend.model.Output;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.TransactionService;
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

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS_2;
import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS_3;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_2;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_4;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_2;
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
        when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH), BTC))
                .thenReturn(Set.of(Transaction.unknown(BTC)));
        assertFailure(Set.of(TRANSACTION_HASH));
    }

    @Test
    void not_sweep_because_of_two_outputs() {
        when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH), BTC)).thenReturn(Set.of(TRANSACTION_2));
        assertFailure(Set.of(TRANSACTION_HASH));
    }

    @Test
    void rejects_two_outputs_if_no_input_output_pair_is_unchanged() {
        Transaction transaction = new Transaction(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                Coins.ofSatoshis(2),
                List.of(new Input(Coins.ofSatoshis(200), ADDRESS), new Input(Coins.ofSatoshis(1_000), ADDRESS)),
                List.of(new Output(Coins.ofSatoshis(999), ADDRESS), new Output(Coins.ofSatoshis(199), ADDRESS)),
                BTC
        );
        when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH), BTC))
                .thenReturn(Set.of(transaction));
        assertFailure(Set.of(TRANSACTION_HASH));
    }

    @Test
    void accepts_several_inputs_and_outputs_if_one_input_output_pair_is_unchanged() {
        int fees = 250;
        // one input (B) is transferred 1:1 while the other inputs (A, C) contribute the fees
        Input inputA = new Input(Coins.ofSatoshis(200), ADDRESS);
        Input inputB = new Input(Coins.ofSatoshis(1_000), ADDRESS);
        Output outputB = new Output(Coins.ofSatoshis(1_000), ADDRESS_2);
        Input inputC = new Input(Coins.ofSatoshis(300), ADDRESS);
        Output outputC = new Output(Coins.ofSatoshis(300 + 200 - fees), ADDRESS_3);
        Transaction transaction = new Transaction(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                Coins.ofSatoshis(fees),
                List.of(inputA, inputB, inputC),
                List.of(outputB, outputC),
                BTC
        );
        when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH), BTC))
                .thenReturn(Set.of(transaction));
        assertThat(sweepTransactionsService.addFromSweeps(Set.of(TRANSACTION_HASH))).isEqualTo(1);
        verify(addressOwnershipService).setAddressAsOwned(ADDRESS_2, BTC);
        verify(addressOwnershipService).setAddressAsOwned(ADDRESS_3, BTC);
        verify(addressDescriptionService).set(ADDRESS_2, DEFAULT_DESCRIPTION);
        verify(addressDescriptionService).set(ADDRESS_3, DEFAULT_DESCRIPTION);
    }

    @Nested
    class Success {
        private static final Input INPUT_SWEEP_1 = new Input(Coins.ofSatoshis(200), new Address("input-address1"));
        private static final Input INPUT_SWEEP_2 = new Input(Coins.ofSatoshis(2), new Address("input-address2"));
        private static final Output OUTPUT_SWEEP_1 = new Output(Coins.ofSatoshis(199), new Address("output-address1"));
        private static final Output OUTPUT_SWEEP_2 = new Output(Coins.ofSatoshis(2), new Address("output-address2"));

        private static final Transaction SWEEP_TRANSACTION = new Transaction(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                Coins.ofSatoshis(1),
                List.of(INPUT_SWEEP_1),
                List.of(OUTPUT_SWEEP_1),
                BTC
        );

        private static final Transaction SWEEP_TRANSACTION_2 = new Transaction(
                TRANSACTION_HASH_2,
                BLOCK_HEIGHT,
                DATE_TIME,
                Coins.ofSatoshis(0),
                List.of(INPUT_SWEEP_2),
                List.of(OUTPUT_SWEEP_2),
                BTC
        );

        private final Set<TransactionHash> hashes = Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2);

        @BeforeEach
        void setUp() {
            when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2), BTC))
                    .thenReturn(Set.of(SWEEP_TRANSACTION, SWEEP_TRANSACTION_2));
        }

        @Test
        void returns_number_of_hashes() {
            assertThat(sweepTransactionsService.addFromSweeps(hashes)).isEqualTo(2);
        }

        @Test
        void accepts_two_inputs() {
            // this may happen for closed channels with unsettled HTLCs or claimed anchors
            when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2), BTC))
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
        void sets_address_descriptions_for_all_inputs() {
            when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2), BTC))
                    .thenReturn(Set.of(TRANSACTION_4));
            sweepTransactionsService.addFromSweeps(hashes);

            TRANSACTION_4.getInputs()
                    .forEach(input -> verify(addressDescriptionService).set(input.getAddress(), DEFAULT_DESCRIPTION));
        }

        @Test
        void sets_address_ownership() {
            sweepTransactionsService.addFromSweeps(hashes);

            verify(addressOwnershipService).setAddressAsOwned(INPUT_SWEEP_1.getAddress(), BTC);
            verify(addressOwnershipService).setAddressAsOwned(INPUT_SWEEP_2.getAddress(), BTC);
            verify(addressOwnershipService).setAddressAsOwned(OUTPUT_SWEEP_1.getAddress(), BTC);
            verify(addressOwnershipService).setAddressAsOwned(OUTPUT_SWEEP_2.getAddress(), BTC);
        }

        @Test
        void sets_address_ownership_for_all_inputs() {
            when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2), BTC))
                    .thenReturn(Set.of(TRANSACTION_4));
            sweepTransactionsService.addFromSweeps(hashes);

            TRANSACTION_4.getInputs()
                    .forEach(input -> verify(addressOwnershipService).setAddressAsOwned(input.getAddress(), BTC));
        }
    }

    private void assertFailure(Set<TransactionHash> hashes) {
        assertThat(sweepTransactionsService.addFromSweeps(hashes)).isEqualTo(0);
        verifyNoInteractions(addressOwnershipService);
    }
}

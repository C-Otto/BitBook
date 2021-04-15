package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.AddressWithDescription;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_4;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LndServiceTest {
    private static final String DEFAULT_DESCRIPTION = "lnd";

    private LndService lndService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionDescriptionService transactionDescriptionService;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Mock
    private AddressOwnershipService addressOwnershipService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper =
                new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        lndService = new LndService(
                transactionService,
                addressDescriptionService,
                transactionDescriptionService,
                addressOwnershipService,
                objectMapper
        );
    }

    @Nested
    class AddFromSweeps {
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

        @Test
        void empty_json() {
            assertFailure("");
        }

        @Test
        void not_json() {
            assertFailure("---");
        }

        @Test
        void empty_json_object() {
            assertFailure("{}");
        }

        @Test
        void no_sweeps() {
            assertFailure("{\"foo\": 1}");
        }

        @Test
        void no_transactionIds() {
            String json = "{\"Sweeps\":{\"foo\": 1}}";
            assertFailure(json);
        }

        @Test
        void no_transaction_ids() {
            String json = "{\"Sweeps\":{\"TransactionIds\": {\"foo\": 1}}}";
            assertFailure(json);
        }

        @Test
        void empty_array() {
            String json = "{\"Sweeps\":{\"TransactionIds\": {\"transaction_ids\": []}}}";
            assertFailure(json);
        }

        @Test
        void unknown_transaction() {
            when(transactionService.getTransactionDetails(TRANSACTION_HASH)).thenReturn(Transaction.UNKNOWN);
            assertFailure(jsonForTransactionHash());
        }

        @Test
        void accepts_two_inputs() {
            // this may happen for closed channels with unsettled UTXOs
            when(addressDescriptionService.get(any()))
                    .then(invocation -> new AddressWithDescription(invocation.getArgument(0)));
            when(transactionService.getTransactionDetails(TRANSACTION_HASH)).thenReturn(TRANSACTION_4);
            assertThat(lndService.lndAddFromSweeps(jsonForTransactionHash())).isEqualTo(1);
        }

        @Test
        void not_sweep_because_of_two_outputs() {
            when(transactionService.getTransactionDetails(TRANSACTION_HASH)).thenReturn(TRANSACTION_2);
            assertFailure(jsonForTransactionHash());
        }

        @Test
        void sweep_transactions() {
            when(addressDescriptionService.get(any()))
                    .then(invocation -> new AddressWithDescription(invocation.getArgument(0)));
            when(transactionService.getTransactionDetails(TRANSACTION_HASH)).thenReturn(SWEEP_TRANSACTION);
            when(transactionService.getTransactionDetails(TRANSACTION_HASH_2)).thenReturn(SWEEP_TRANSACTION_2);
            String json = "{\"Sweeps\":{\"TransactionIds\": {\"transaction_ids\": [\"%s\", \"%s\"]}}}"
                    .formatted(TRANSACTION_HASH, TRANSACTION_HASH_2);

            assertThat(lndService.lndAddFromSweeps(json)).isEqualTo(2);

            verify(transactionDescriptionService).set(TRANSACTION_HASH, "lnd sweep transaction");
            verify(transactionDescriptionService).set(TRANSACTION_HASH_2, "lnd sweep transaction");
            verify(addressDescriptionService).set(INPUT_SWEEP_1.getAddress(), DEFAULT_DESCRIPTION);
            verify(addressDescriptionService).set(INPUT_SWEEP_2.getAddress(), DEFAULT_DESCRIPTION);
            verify(addressDescriptionService).set(OUTPUT_SWEEP_1.getAddress(), DEFAULT_DESCRIPTION);
            verify(addressDescriptionService).set(OUTPUT_SWEEP_2.getAddress(), DEFAULT_DESCRIPTION);
            verify(addressOwnershipService).setAddressAsOwned(INPUT_SWEEP_1.getAddress());
            verify(addressOwnershipService).setAddressAsOwned(INPUT_SWEEP_2.getAddress());
            verify(addressOwnershipService).setAddressAsOwned(OUTPUT_SWEEP_1.getAddress());
            verify(addressOwnershipService).setAddressAsOwned(OUTPUT_SWEEP_2.getAddress());
        }

        @Test
        void does_not_overwrite_source_description() {
            when(addressDescriptionService.get(INPUT_SWEEP_1.getAddress()))
                    .thenReturn(new AddressWithDescription(INPUT_SWEEP_1.getAddress(), "do-not-overwrite-me"));
            when(transactionService.getTransactionDetails(TRANSACTION_HASH)).thenReturn(SWEEP_TRANSACTION);

            lndService.lndAddFromSweeps(jsonForTransactionHash());

            verify(transactionDescriptionService, never()).set(eq(INPUT_SWEEP_1.getAddress()), any());
        }

        private String jsonForTransactionHash() {
            return "{\"Sweeps\":{\"TransactionIds\": {\"transaction_ids\": [\"%s\"]}}}"
                    .formatted(TRANSACTION_HASH);
        }

        private void assertFailure(String json) {
            assertThat(lndService.lndAddFromSweeps(json)).isEqualTo(0);
            verifyNoInteractions(addressOwnershipService);
        }
    }

    @Nested
    class AddFromUnspentOutputs {
        @Test
        void empty_json() {
            assertFailure("");
        }

        @Test
        void not_json() {
            assertFailure("---");
        }

        @Test
        void empty_json_object() {
            assertFailure("{}");
        }

        @Test
        void no_utxos() {
            assertFailure("{\"foo\": 1}");
        }

        @Test
        void empty_array() {
            String json = "{\"utxos\":[]}";
            assertFailure(json);
        }

        @Test
        void unconfirmed_transaction() {
            String json = "{\"utxos\":[{\"address\":\"bc1qngw83\",\"confirmations\": 0}]}";
            assertFailure(json);
        }

        @Test
        void success() {
            String json = "{\"utxos\":[" +
                          "{\"address\":\"bc1qngw83\",\"confirmations\": 123}, " +
                          "{\"address\":\"bc1aaaaaa\",\"confirmations\":597}" +
                          "]}";
            assertThat(lndService.lndAddUnspentOutputs(json)).isEqualTo(2);
            verify(addressOwnershipService).setAddressAsOwned("bc1qngw83");
            verify(addressOwnershipService).setAddressAsOwned("bc1aaaaaa");
            verify(addressDescriptionService).set("bc1qngw83", DEFAULT_DESCRIPTION);
            verify(addressDescriptionService).set("bc1aaaaaa", DEFAULT_DESCRIPTION);
        }

        private void assertFailure(String json) {
            assertThat(lndService.lndAddUnspentOutputs(json)).isEqualTo(0);
            verifyNoInteractions(addressOwnershipService);
        }
    }
}
package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.lnd.features.ClosedChannelsService;
import de.cotto.bitbook.lnd.features.SweepTransactionsService;
import de.cotto.bitbook.lnd.features.UnspentOutputsService;
import de.cotto.bitbook.lnd.model.ClosedChannel;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.CLOSING_TRANSACTION;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.OPENING_TRANSACTION;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.RESOLUTION_AMOUNT;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.SWEEP_TRANSACTION_HASH;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.WITH_RESOLUTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LndServiceTest {
    private LndService lndService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AddressOwnershipService addressOwnershipService;

    @Mock
    private ClosedChannelsService closedChannelsService;

    @Mock
    private UnspentOutputsService unspentOutputsService;

    @Mock
    private SweepTransactionsService sweepTransactionsService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper =
                new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        lndService = new LndService(
                objectMapper,
                closedChannelsService,
                unspentOutputsService,
                sweepTransactionsService,
                transactionService
        );
    }

    @Nested
    class AddFromSweeps {
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
        void success() {
            when(sweepTransactionsService.addFromSweeps(Set.of("a", "b"))).thenReturn(2L);
            String json = "{\"Sweeps\":{\"TransactionIds\": {\"transaction_ids\": [\"a\", \"b\"]}}}";
            assertThat(lndService.addFromSweeps(json)).isEqualTo(2);
        }

        private void assertFailure(String json) {
            assertThat(lndService.addFromSweeps(json)).isEqualTo(0);
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
            when(unspentOutputsService.addFromUnspentOutputs(Set.of("bc1qngw83", "bc1aaaaaa"))).thenReturn(2L);
            String json = "{\"utxos\":[" +
                          "{\"address\":\"bc1qngw83\",\"confirmations\":123}, " +
                          "{\"address\":\"bc1aaaaaa\",\"confirmations\":597}" +
                          "]}";
            assertThat(lndService.addFromUnspentOutputs(json)).isEqualTo(2);
        }

        private void assertFailure(String json) {
            assertThat(lndService.addFromUnspentOutputs(json)).isEqualTo(0);
            verifyNoInteractions(addressOwnershipService);
        }
    }

    @Nested
    class AddFromClosedChannels {
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
        void no_channels() {
            assertFailure("{\"foo\": 1}");
        }

        @Test
        void not_array() {
            String json = "{\"channels\":1}";
            assertFailure(json);
        }

        @Test
        void skips_channels_with_unconfirmed_close_transactions() {
            int closeHeight = 0;
            String json = getJsonArrayWithSingleChannel("").replace(
                    String.valueOf(BLOCK_HEIGHT),
                    String.valueOf(closeHeight)
            );

            lndService.addFromClosedChannels(json);

            verify(closedChannelsService).addFromClosedChannels(Set.of());
            verifyNoInteractions(transactionService);
        }

        @Test
        void skips_channels_with_unknown_close_transactions() {
            String closingTransactionHash = "0000000000000000000000000000000000000000000000000000000000000000";
            String json = getJsonArrayWithSingleChannel("").replace(TRANSACTION_HASH_2, closingTransactionHash);

            lndService.addFromClosedChannels(json);

            verify(closedChannelsService).addFromClosedChannels(Set.of());
            verifyNoInteractions(transactionService);
        }

        @Test
        void success() {
            when(transactionService.getTransactionDetails(TRANSACTION_HASH)).thenReturn(OPENING_TRANSACTION);
            when(transactionService.getTransactionDetails(TRANSACTION_HASH_2)).thenReturn(CLOSING_TRANSACTION);
            ClosedChannel closedChannel2 = CLOSED_CHANNEL.toBuilder().withSettledBalance(Coins.ofSatoshis(500)).build();
            when(closedChannelsService.addFromClosedChannels(Set.of(CLOSED_CHANNEL, closedChannel2))).thenReturn(2L);

            long result = lndService.addFromClosedChannels(
                    "{\"channels\": [" +
                    getJsonSingleClosedChannel(CLOSED_CHANNEL.getSettledBalance(), "") +
                    "," +
                    getJsonSingleClosedChannel(closedChannel2.getSettledBalance(), "") +
                    "]}"
            );

            assertThat(result).isEqualTo(2);
        }

        @Test
        void with_resolution() {
            when(transactionService.getTransactionDetails(TRANSACTION_HASH)).thenReturn(OPENING_TRANSACTION);
            when(transactionService.getTransactionDetails(TRANSACTION_HASH_2)).thenReturn(CLOSING_TRANSACTION);
            when(closedChannelsService.addFromClosedChannels(Set.of(WITH_RESOLUTION))).thenReturn(1L);

            long result = lndService.addFromClosedChannels(
                    getJsonArrayWithSingleChannel("{" +
                                                  "\"sweep_txid\": \"" + SWEEP_TRANSACTION_HASH + "\"," +
                                                  "\"amount_sat\": \"" + RESOLUTION_AMOUNT.getSatoshis() + "\"" +
                                                  "}")
            );

            assertThat(result).isEqualTo(1L);
        }

        private String getJsonArrayWithSingleChannel(String resolutions) {
            return "{\"channels\": [" +
                   getJsonSingleClosedChannel(CLOSED_CHANNEL.getSettledBalance(), resolutions)
                   + "]}";
        }

        private String getJsonSingleClosedChannel(Coins settledBalance, String resolutions) {
            return "{" +
                   "\"channel_point\": \"" + TRANSACTION_HASH + ":123\"," +
                   "\"closing_tx_hash\": \"" + TRANSACTION_HASH_2 + "\"," +
                   "\"remote_pubkey\": \"pubkey\"," +
                   "\"chain_hash\": \"000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f\"," +
                   "\"settled_balance\": \"" + settledBalance.getSatoshis() + "\"," +
                   "\"close_height\": 601164," +
                   "\"close_type\": \"COOPERATIVE_CLOSE\"," +
                   "\"open_initiator\": \"INITIATOR_REMOTE\"," +
                   "\"close_initiator\": \"INITIATOR_REMOTE\"," +
                   "\"resolutions\": [" + resolutions + "]" +
                   "}";
        }

        private void assertFailure(String json) {
            assertThat(lndService.addFromClosedChannels(json)).isEqualTo(0);
            verifyNoInteractions(addressOwnershipService);
        }
    }
}
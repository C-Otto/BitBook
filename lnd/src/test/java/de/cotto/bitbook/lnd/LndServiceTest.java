package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.lnd.features.ChannelsService;
import de.cotto.bitbook.lnd.features.ClosedChannelsService;
import de.cotto.bitbook.lnd.features.OnchainTransactionsService;
import de.cotto.bitbook.lnd.features.SweepTransactionsService;
import de.cotto.bitbook.lnd.features.UnspentOutputsService;
import de.cotto.bitbook.lnd.model.OnchainTransaction;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.bitbook.lnd.model.ChannelFixtures.CHANNEL;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.bitbook.lnd.model.OnchainTransactionFixtures.ONCHAIN_TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LndServiceTest {
    private LndService lndService;

    @Mock
    private AddressOwnershipService addressOwnershipService;

    @Mock
    private ClosedChannelsService closedChannelsService;

    @Mock
    private UnspentOutputsService unspentOutputsService;

    @Mock
    private SweepTransactionsService sweepTransactionsService;

    @Mock
    private ClosedChannelsParser closedChannelsParser;

    @Mock
    private ChannelsService channelsService;

    @Mock
    private ChannelsParser channelsParser;

    @Mock
    private OnchainTransactionsParser onchainTransactionsParser;

    @Mock
    private OnchainTransactionsService onchainTransactionsService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper =
                new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        lndService = new LndService(
                objectMapper,
                closedChannelsService,
                unspentOutputsService,
                sweepTransactionsService,
                closedChannelsParser,
                channelsService,
                channelsParser,
                onchainTransactionsParser,
                onchainTransactionsService
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
            assertFailure("-x--");
        }

        @Test
        void empty_json_object() {
            assertFailure("{}");
        }

        @Test
        void no_sweeps() {
            assertFailure("{\"hello\": 2}");
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
    class AddFromChannels {
        @Test
        void empty_json() {
            assertThat(lndService.addFromChannels("")).isEqualTo(0);
            verifyNoInteractions(channelsParser);
        }

        @Test
        void not_json() {
            assertThat(lndService.addFromChannels("NOT JSON")).isEqualTo(0);
            verifyNoInteractions(channelsParser);
        }

        @Test
        void parses_json() {
            lndService.addFromChannels("{\"valid\": \"json\"}");
            verify(channelsParser).parse(argThat(node -> "{\"valid\":\"json\"}".equals(node.toString())));
        }

        @Test
        void calls_service() {
            when(channelsParser.parse(any())).thenReturn(Set.of(CHANNEL));
            lndService.addFromChannels("{}");
            verify(channelsService).addFromChannels(Set.of(CHANNEL));
        }
    }

    @Nested
    class AddFromClosedChannels {
        @Test
        void empty_json() {
            assertThat(lndService.addFromClosedChannels("")).isEqualTo(0);
            verifyNoInteractions(closedChannelsParser);
        }

        @Test
        void not_json() {
            assertThat(lndService.addFromClosedChannels("---")).isEqualTo(0);
            verifyNoInteractions(closedChannelsParser);
        }

        @Test
        void parses_json() {
            lndService.addFromClosedChannels("{\"foo\": 1}");
            verify(closedChannelsParser).parse(argThat(node -> "{\"foo\":1}".equals(node.toString())));
        }

        @Test
        void calls_service() {
            when(closedChannelsParser.parse(any())).thenReturn(Set.of(CLOSED_CHANNEL));
            lndService.addFromClosedChannels("{}");
            verify(closedChannelsService).addFromClosedChannels(Set.of(CLOSED_CHANNEL));
        }
    }

    @Nested
    class AddFromOnchainTransactions {
        @Test
        void empty_json() {
            assertThat(lndService.addFromOnchainTransactions("")).isEqualTo(0);
        }

        @Test
        void not_json() {
            assertThat(lndService.addFromOnchainTransactions("---")).isEqualTo(0);
        }

        @Test
        void parses_json() {
            lndService.addFromOnchainTransactions("{\"hello\": \"world\"}");
            verify(onchainTransactionsParser).parse(argThat(node -> "{\"hello\":\"world\"}".equals(node.toString())));
        }

        @Test
        void calls_service() {
            Set<OnchainTransaction> onchainTransactions = Set.of(ONCHAIN_TRANSACTION);
            when(onchainTransactionsParser.parse(any())).thenReturn(onchainTransactions);
            lndService.addFromOnchainTransactions("{}");
            verify(onchainTransactionsService).addFromOnchainTransactions(onchainTransactions);
        }
    }
}
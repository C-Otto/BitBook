package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.lnd.model.ClosedChannel;
import de.cotto.bitbook.lnd.model.Resolution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Set;

import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.CLOSING_TRANSACTION;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.OPENING_TRANSACTION;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.RESOLUTION_AMOUNT;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.SWEEP_TRANSACTION_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClosedChannelsParserTest {
    private static final String RESOLUTION_WITH_HASH =
            "{" +
            "\"sweep_txid\": \"" + SWEEP_TRANSACTION_HASH + "\"" +
            ", \"amount_sat\": \"" + RESOLUTION_AMOUNT.getSatoshis() + "\"" +
            ", \"resolution_type\": \"COMMIT\"" +
            ", \"outcome\": \"CLAIMED\"" +
            "}";

    @InjectMocks
    private ClosedChannelsParser closedChannelsParser;

    @Mock
    private TransactionService transactionService;

    @Test
    void empty_json_object() throws IOException {
        assertThat(closedChannelsParser.parse(toJsonNode("{}"))).isEmpty();
    }

    @Test
    void no_channels() throws IOException {
        assertThat(closedChannelsParser.parse(toJsonNode("{\"foo\": 1}"))).isEmpty();
    }

    @Test
    void not_array() throws IOException {
        String json = "{\"channels\":1}";
        assertThat(closedChannelsParser.parse(toJsonNode(json))).isEmpty();
    }

    @Test
    void skips_channels_with_unconfirmed_close_transactions() throws IOException {
        int closeHeight = 0;
        String json = getJsonArrayWithSingleChannel("").replace(
                String.valueOf(BLOCK_HEIGHT),
                String.valueOf(closeHeight)
        );

        assertThat(closedChannelsParser.parse(toJsonNode(json))).isEmpty();
        verify(transactionService, never()).getTransactionDetails(any(TransactionHash.class));
    }

    @Test
    void skips_channels_with_unknown_close_transactions() throws IOException {
        String closingTransactionHash = "0000000000000000000000000000000000000000000000000000000000000000";
        String json = getJsonArrayWithSingleChannel("").replace(TRANSACTION_HASH_2.toString(), closingTransactionHash);

        assertThat(closedChannelsParser.parse(toJsonNode(json))).isEmpty();
        verify(transactionService, never()).getTransactionDetails(any(TransactionHash.class));
    }

    @Test
    void success() throws IOException {
        mockTransactionDetails();
        ClosedChannel closedChannel2 = CLOSED_CHANNEL.toBuilder().withSettledBalance(Coins.ofSatoshis(500)).build();

        assertThat(closedChannelsParser.parse(toJsonNode(
                "{\"channels\": [" +
                getJsonSingleClosedChannel(CLOSED_CHANNEL.getSettledBalance(), "") +
                "," +
                getJsonSingleClosedChannel(closedChannel2.getSettledBalance(), "") +
                "]}"
        ))).hasSize(2);
    }

    @Test
    void preloads_transaction_details() throws IOException {
        mockTransactionDetails();
        ClosedChannel closedChannel2 = CLOSED_CHANNEL.toBuilder().withSettledBalance(Coins.ofSatoshis(500)).build();

        closedChannelsParser.parse(toJsonNode(
                "{\"channels\": [" +
                getJsonSingleClosedChannel(CLOSED_CHANNEL.getSettledBalance(), "") +
                "," +
                getJsonSingleClosedChannel(closedChannel2.getSettledBalance(), "") +
                "]}"
        ));
        InOrder inOrder = Mockito.inOrder(transactionService);
        inOrder.verify(transactionService).getTransactionDetails(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2));
        inOrder.verify(transactionService, atLeastOnce()).getTransactionDetails(any(TransactionHash.class));
    }

    @Test
    void with_resolution() throws IOException {
        mockTransactionDetails();

        Set<ClosedChannel> closedChannels = closedChannelsParser.parse(toJsonNode(
                getJsonArrayWithSingleChannel(RESOLUTION_WITH_HASH)
        ));
        assertThat(closedChannels).hasSize(1)
                .flatMap(ClosedChannel::getResolutions)
                .map(Resolution::sweepTransactionHash)
                .contains(SWEEP_TRANSACTION_HASH);
    }

    @Test
    void with_resolution_includes_resolution_type() throws IOException {
        mockTransactionDetails();

        Set<ClosedChannel> closedChannels = closedChannelsParser.parse(toJsonNode(
                getJsonArrayWithSingleChannel(RESOLUTION_WITH_HASH)
        ));
        assertThat(closedChannels).hasSize(1)
                .flatMap(ClosedChannel::getResolutions)
                .map(Resolution::resolutionType)
                .contains("COMMIT");
    }

    @Test
    void with_resolution_includes_outcome() throws IOException {
        mockTransactionDetails();

        Set<ClosedChannel> closedChannels = closedChannelsParser.parse(toJsonNode(
                getJsonArrayWithSingleChannel(RESOLUTION_WITH_HASH)
        ));
        assertThat(closedChannels).hasSize(1)
                .flatMap(ClosedChannel::getResolutions)
                .map(Resolution::outcome)
                .contains("CLAIMED");
    }

    private void mockTransactionDetails() {
        when(transactionService.getTransactionDetails(anySet())).thenReturn(Set.of());
        when(transactionService.getTransactionDetails(TRANSACTION_HASH)).thenReturn(OPENING_TRANSACTION);
        when(transactionService.getTransactionDetails(TRANSACTION_HASH_2)).thenReturn(CLOSING_TRANSACTION);
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

    private JsonNode toJsonNode(String json) throws IOException {
        try (JsonParser parser = new ObjectMapper().createParser(json)) {
            return parser.getCodec().readTree(parser);
        }
    }
}
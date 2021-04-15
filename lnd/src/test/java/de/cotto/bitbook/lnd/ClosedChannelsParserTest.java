package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.lnd.model.ClosedChannel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.CLOSING_TRANSACTION;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.OPENING_TRANSACTION;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.RESOLUTION_AMOUNT;
import static de.cotto.bitbook.lnd.model.ClosedChannelFixtures.SWEEP_TRANSACTION_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClosedChannelsParserTest {
    @InjectMocks
    private ClosedChannelsParser closedChannelsParser;

    @Mock
    private TransactionService transactionService;

    @Test
    void empty_json_object() throws IOException {
        assertFailure("{}");
    }

    @Test
    void no_channels() throws IOException {
        assertFailure("{\"foo\": 1}");
    }

    @Test
    void not_array() throws IOException {
        String json = "{\"channels\":1}";
        assertFailure(json);
    }

    @Test
    void skips_channels_with_unconfirmed_close_transactions() throws IOException {
        int closeHeight = 0;
        String json = getJsonArrayWithSingleChannel("").replace(
                String.valueOf(BLOCK_HEIGHT),
                String.valueOf(closeHeight)
        );

        assertThat(closedChannelsParser.parse(toJsonNode(json))).isEmpty();
        verifyNoInteractions(transactionService);
    }

    @Test
    void skips_channels_with_unknown_close_transactions() throws IOException {
        String closingTransactionHash = "0000000000000000000000000000000000000000000000000000000000000000";
        String json = getJsonArrayWithSingleChannel("").replace(TRANSACTION_HASH_2, closingTransactionHash);

        assertThat(closedChannelsParser.parse(toJsonNode(json))).isEmpty();
        verifyNoInteractions(transactionService);
    }

    @Test
    void success() throws IOException {
        when(transactionService.getTransactionDetails(TRANSACTION_HASH)).thenReturn(OPENING_TRANSACTION);
        when(transactionService.getTransactionDetails(TRANSACTION_HASH_2)).thenReturn(CLOSING_TRANSACTION);
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
    void with_resolution() throws IOException {
        when(transactionService.getTransactionDetails(TRANSACTION_HASH)).thenReturn(OPENING_TRANSACTION);
        when(transactionService.getTransactionDetails(TRANSACTION_HASH_2)).thenReturn(CLOSING_TRANSACTION);

        assertThat(closedChannelsParser.parse(toJsonNode(
                getJsonArrayWithSingleChannel("{" +
                                              "\"sweep_txid\": \"" + SWEEP_TRANSACTION_HASH + "\"," +
                                              "\"amount_sat\": \"" + RESOLUTION_AMOUNT.getSatoshis() + "\"" +
                                              "}")
        ))).hasSize(1);
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

    private void assertFailure(String json) throws IOException {
        assertThat(closedChannelsParser.parse(toJsonNode(json))).isEmpty();
    }

    private JsonNode toJsonNode(String json) throws IOException {
        try (JsonParser parser = new ObjectMapper().createParser(json)) {
            return parser.getCodec().readTree(parser);
        }
    }
}
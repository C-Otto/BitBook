package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.lnd.model.OnchainTransaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS_2;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OnchainTransactionsParserTest {
    @InjectMocks
    private OnchainTransactionsParser onchainTransactionsParser;

    @Test
    void no_transactions() throws IOException {
        assertThat(onchainTransactionsParser.parse(toJsonNode("{}"))).isEmpty();
    }

    @Test
    void not_array() throws IOException {
        assertThat(onchainTransactionsParser.parse(toJsonNode("{\"transactions\": 1}"))).isEmpty();
    }

    @Test
    void two_transactions() throws IOException {
        String json = "{\"transactions\":[" +
                "{\"tx_hash\": \"a\", \"total_fees\": \"12\", \"amount\": \"900\", \"label\": \"\"}," +
                "{\"tx_hash\": \"b\", \"total_fees\": \"0\", \"amount\": \"111\", \"label\": \"xxx\"}" +
                "]}";
        assertThat(onchainTransactionsParser.parse(toJsonNode(json))).containsExactlyInAnyOrder(
                new OnchainTransaction(new TransactionHash("a"), "", Coins.ofSatoshis(900), Coins.ofSatoshis(12)),
                new OnchainTransaction(new TransactionHash("b"), "xxx", Coins.ofSatoshis(111), Coins.NONE)
        );
    }

    @Test
    void owned_address_from_output_details() throws IOException {
        String json = """
                {
                  "transactions": [
                    {
                      "tx_hash": "a",
                      "total_fees": "12",
                      "amount": "900",
                      "label": "",
                      "output_details": [
                        {
                          "address": "%s",
                          "is_our_address": false
                        },
                        {
                          "address": "%s",
                          "is_our_address": true
                        }
                      ]
                    }
                  ]
                }""".formatted(ADDRESS, ADDRESS_2);
        assertThat(onchainTransactionsParser.parse(toJsonNode(json))).containsExactly(
                new OnchainTransaction(
                        new TransactionHash("a"),
                        "",
                        Coins.ofSatoshis(900),
                        Coins.ofSatoshis(12),
                        Set.of(ADDRESS_2)
                )
        );
    }

    private JsonNode toJsonNode(String json) throws IOException {
        try (JsonParser parser = new ObjectMapper().createParser(json)) {
            return parser.getCodec().readTree(parser);
        }
    }
}

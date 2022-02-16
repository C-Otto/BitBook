package de.cotto.bitbook.backend.transaction.electrs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.electrs.jsonrpc.JsonRpcClient;
import de.cotto.bitbook.backend.transaction.electrs.jsonrpc.JsonRpcMessage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static org.assertj.core.api.Assertions.assertThat;

class ElectrsClientTest {
    private static final String SUBSCRIBE_PATTERN = "\\[\\{" +
            "\"method\":\"blockchain.scripthash.subscribe\"," +
            "\"params\":\\[\".*\"]," +
            "\"id\":\\d+," +
            "\"jsonrpc\":\"2.0\"" +
            "}]";
    private static final String GET_HISTORY_PATTERN = "\\[\\{" +
            "\"method\":\"blockchain.scripthash.get_history\"," +
            "\"params\":\\[\".*\"]," +
            "\"id\":\\d+," +
            "\"jsonrpc\":\"2.0\"" +
            "}]";
    private final TestableJsonRpcClient jsonRpcClient = new TestableJsonRpcClient();
    private final ElectrsClient electrsClient = new ElectrsClient(jsonRpcClient);

    @Test
    void subscribes_and_requests_history() {
        electrsClient.getTransactionHashes(ADDRESS);
        assertThat(jsonRpcClient.messages.get(0).toString()).matches(SUBSCRIBE_PATTERN);
        assertThat(jsonRpcClient.messages.get(1).toString()).matches(GET_HISTORY_PATTERN);
    }

    @Test
    void uses_reversed_sha256_param_for_script() {
        electrsClient.getTransactionHashes(ADDRESS);
        String expectedParameter = "2bffa1631b6d8daac73ea21a55749b3f979f0054172bc4620307fd2992b9ea5c";
        String patternWithScriptParameter = ".*\\[\"%s\"].*".formatted(expectedParameter);
        assertThat(jsonRpcClient.messages.get(0).toString()).matches(patternWithScriptParameter);
        assertThat(jsonRpcClient.messages.get(1).toString()).matches(patternWithScriptParameter);
    }

    @Test
    void returns_confirmed_hashes() {
        Optional<Set<TransactionHash>> result = electrsClient.getTransactionHashes(ADDRESS);
        assertThat(result.orElseThrow()).containsExactlyInAnyOrder(new TransactionHash("a"), new TransactionHash("b"));
    }

    @Test
    void empty_on_failure() {
        jsonRpcClient.fail = true;
        Optional<Set<TransactionHash>> result = electrsClient.getTransactionHashes(ADDRESS);
        assertThat(result).isEmpty();
    }

    private static class TestableJsonRpcClient extends JsonRpcClient {
        public boolean fail;
        private List<JsonRpcMessage> messages = List.of();
        private final ObjectMapper objectMapper = new ObjectMapper();

        public TestableJsonRpcClient() {
            super("", 0);
        }

        @Override
        public void sendMessages(List<JsonRpcMessage> messages) {
            if (fail) {
                messages.forEach(JsonRpcMessage::cancel);
            } else {
                messages.forEach(m -> m.responseReceived(response(m.getMessageId())));
            }
            this.messages = messages;
        }

        private JsonNode response(int messageId) {
            ObjectNode node = objectMapper.createObjectNode();
            ArrayNode hashes = objectMapper.createArrayNode();
            hashes.add(hashNode("a", BLOCK_HEIGHT));
            hashes.add(hashNode("b", BLOCK_HEIGHT - 1));
            hashes.add(hashNode("c", 0));
            node.put("id", messageId);
            node.set("result", hashes);
            return node;
        }

        private ObjectNode hashNode(String txHash, int height) {
            ObjectNode hashNode = objectMapper.createObjectNode();
            hashNode.put("tx_hash", txHash);
            hashNode.put("height", height);
            return hashNode;
        }
    }
}
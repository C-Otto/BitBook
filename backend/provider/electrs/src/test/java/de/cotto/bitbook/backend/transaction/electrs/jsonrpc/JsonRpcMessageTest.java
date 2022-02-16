package de.cotto.bitbook.backend.transaction.electrs.jsonrpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class JsonRpcMessageTest {

    private JsonRpcMessage message;
    private CompletableFuture<JsonNode> future;

    @BeforeEach
    void setUp() {
        message = new JsonRpcMessage("method");
        future = message.getFuture();
    }

    @Test
    void testToString_no_parameter() {
        assertThat(new JsonRpcMessage("method").toString())
                .matches("\\[\\{\"method\":\"method\",\"params\":\\[],\"id\":\\d+,\"jsonrpc\":\"2.0\"}]");
    }

    @Test
    void testToString_one_parameter() {
        assertThat(new JsonRpcMessage("m", "param").toString())
                .matches("\\[\\{\"method\":\"m\",\"params\":\\[\"param\"],\"id\":\\d+,\"jsonrpc\":\"2.0\"}]");
    }

    @Test
    void testToString_several_weird_parameters() {
        assertThat(new JsonRpcMessage("m", "p1", "p 2", "p\"3").toString())
                .matches(".*\"params\":\\[\"p1\",\"p 2\",\"p\\\\\"3\"].*");
    }

    @Test
    void messages_have_different_ids() {
        JsonRpcMessage message1 = new JsonRpcMessage("m");
        JsonRpcMessage message2 = new JsonRpcMessage("m");
        assertThat(message1.toString()).isNotEqualTo(message2.toString());
    }

    @Test
    void responseReceived_wrong_id() {
        message.responseReceived(response(message.getMessageId() + 1));
        assertThat(message.isDone()).isFalse();
    }

    @Test
    void responseReceived_correct_id() {
        message.responseReceived(response(message.getMessageId()));
        assertThat(message.isDone()).isTrue();
        assertThat(future.getNow(null)).hasToString("{\"id\":" + message.getMessageId() + "}");
    }

    @Test
    void isDone_initial() {
        assertThat(message.isDone()).isFalse();
    }

    @Test
    void isDone_cancelled() {
        message.cancel();
        assertThat(message.isDone()).isTrue();
    }

    @Test
    void cancel() {
        message.cancel();
        assertThat(future.isCancelled()).isTrue();
    }

    private JsonNode response(int messageId) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("id", messageId);
        return node;
    }
}
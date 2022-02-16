package de.cotto.bitbook.backend.transaction.electrs.jsonrpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class JsonRpcMessage {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger();
    private final int messageId;
    private final String method;
    private final List<String> parameters;
    private final CompletableFuture<JsonNode> future;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonRpcMessage(String method, Object... parameters) {
        future = new CompletableFuture<>();
        messageId = ID_COUNTER.incrementAndGet();
        this.method = method;
        this.parameters = Arrays.stream(parameters).map(Object::toString).toList();
    }

    public void responseReceived(JsonNode response) {
        if (response.get("id").intValue() == messageId) {
            future.complete(response);
        }
    }

    @Override
    public String toString() {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("method", method);
        ArrayNode parametersNode = objectMapper.createArrayNode();
        parameters.forEach(parametersNode::add);
        objectNode.set("params", parametersNode);
        objectNode.put("id", messageId);
        objectNode.put("jsonrpc", "2.0");
        arrayNode.add(objectNode);
        try {
            return objectMapper.writeValueAsString(arrayNode);
        } catch (JsonProcessingException exception) {
            return "";
        }
    }

    public void cancel() {
        future.cancel(true);
    }

    public int getMessageId() {
        return messageId;
    }

    public CompletableFuture<JsonNode> getFuture() {
        return future;
    }

    @JsonIgnore
    public boolean isDone() {
        return future.isDone();
    }
}

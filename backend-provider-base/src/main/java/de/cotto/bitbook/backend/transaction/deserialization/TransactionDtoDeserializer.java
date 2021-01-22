package de.cotto.bitbook.backend.transaction.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

public abstract class TransactionDtoDeserializer<T extends TransactionDto>
        extends JsonDeserializer<T> {

    private final String hashProperty;
    private final String blockHeightProperty;
    private final String feesProperty;
    private final String timeProperty;
    private final InputOutputDtoDeserializer inputOutputDeserializer;

    protected TransactionDtoDeserializer(
            String hashProperty,
            String blockHeightProperty,
            String feesProperty,
            String timeProperty,
            InputOutputDtoDeserializer inputOutputDeserializer
    ) {
        super();
        this.hashProperty = hashProperty;
        this.blockHeightProperty = blockHeightProperty;
        this.feesProperty = feesProperty;
        this.timeProperty = timeProperty;
        this.inputOutputDeserializer = inputOutputDeserializer;
    }

    @Override
    public T deserialize(
            JsonParser jsonParser,
            DeserializationContext deserializationContext
    ) throws IOException {
        JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
        if (isUnsupported(rootNode)) {
            throw new IllegalStateException();
        }
        Set<JsonNode> transactionNodes = getTransactionNodes(rootNode);
        return transactionNodes.stream()
                .findFirst()
                .map(this::getTransactionDto)
                .orElseThrow();
    }

    protected boolean isUnsupported(JsonNode rootNode) {
        return false;
    }

    protected Set<JsonNode> getTransactionNodes(JsonNode rootNode) {
        return Set.of(rootNode);
    }

    protected JsonNode getTransactionDetailsNode(JsonNode transactionNode) {
        return transactionNode;
    }

    protected abstract boolean isCoinbaseTransaction(JsonNode transactionNode);

    protected abstract T createDtoInstance(
            String hash,
            int blockHeight,
            LocalDateTime time,
            long fees,
            List<InputDto> inputs,
            List<OutputDto> outputs
    );

    private T getTransactionDto(JsonNode transactionNode) {
        JsonNode transactionDetailsNode = getTransactionDetailsNode(transactionNode);
        String hash = transactionDetailsNode.get(hashProperty).textValue();
        int blockHeight = transactionDetailsNode.get(blockHeightProperty).asInt();
        LocalDateTime parsedTime = parseTime(transactionDetailsNode.get(timeProperty).asText());
        long fees = transactionDetailsNode.get(feesProperty).asLong();
        List<InputDto> inputs = getInputs(transactionNode);
        List<OutputDto> outputs = getOutputs(transactionNode);
        return createDtoInstance(hash, blockHeight, parsedTime, fees, inputs, outputs);
    }

    protected LocalDateTime parseTime(String time) {
        return LocalDateTime.ofEpochSecond(Long.parseLong(time), 0, ZoneOffset.UTC);
    }

    private List<InputDto> getInputs(JsonNode transactionNode) {
        if (isCoinbaseTransaction(transactionNode)) {
            return List.of(InputDto.COINBASE);
        }
        return inputOutputDeserializer.getInputs(transactionNode);
    }

    private List<OutputDto> getOutputs(JsonNode transactionNode) {
        return inputOutputDeserializer.getOutputs(transactionNode);
    }
}

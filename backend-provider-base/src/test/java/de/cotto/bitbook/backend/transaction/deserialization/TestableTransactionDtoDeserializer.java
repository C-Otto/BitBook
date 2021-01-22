package de.cotto.bitbook.backend.transaction.deserialization;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toSet;

public class TestableTransactionDtoDeserializer extends TransactionDtoDeserializer<TestableTransactionDto> {
    protected TestableTransactionDtoDeserializer() {
        super(
                "this-is-the-hash",
                "blockheight",
                "fees",
                "received",
                new DefaultInputOutputDtoDeserializer(List.of("inputs"), List.of("outputs"), "val", "val", "addr")
        );
    }

    @Override
    protected Set<JsonNode> getTransactionNodes(JsonNode rootNode) {
        if (rootNode.has("hidden_in_here")) {
            return StreamSupport.stream(rootNode.get("hidden_in_here").spliterator(), false).collect(toSet());
        }
        return super.getTransactionNodes(rootNode);
    }

    @Override
    protected JsonNode getTransactionDetailsNode(JsonNode transactionNode) {
        if (transactionNode.has("details")) {
            return transactionNode.get("details");
        }
        return super.getTransactionDetailsNode(transactionNode);
    }

    @Override
    protected boolean isCoinbaseTransaction(JsonNode transactionNode) {
        return transactionNode.has("coinbase");
    }

    @Override
    protected boolean isUnsupported(JsonNode rootNode) {
        if (rootNode.has("unsupported")) {
            return true;
        }
        return super.isUnsupported(rootNode);
    }

    @Override
    protected TestableTransactionDto createDtoInstance(
            String hash,
            int blockHeight,
            LocalDateTime time,
            long fees,
            List<InputDto> inputs,
            List<OutputDto> outputs
    ) {
        return new TestableTransactionDto(hash, blockHeight, time, fees, inputs, outputs);
    }
}

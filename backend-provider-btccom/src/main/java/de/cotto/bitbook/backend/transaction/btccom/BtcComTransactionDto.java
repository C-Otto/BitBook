package de.cotto.bitbook.backend.transaction.btccom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cotto.bitbook.backend.transaction.deserialization.InputDto;
import de.cotto.bitbook.backend.transaction.deserialization.MultiAddressInputOutputDtoDeserializer;
import de.cotto.bitbook.backend.transaction.deserialization.OutputDto;
import de.cotto.bitbook.backend.transaction.deserialization.TransactionDto;
import de.cotto.bitbook.backend.transaction.deserialization.TransactionDtoDeserializer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@JsonDeserialize(using = BtcComTransactionDto.Deserializer.class)
public class BtcComTransactionDto extends TransactionDto {
    public BtcComTransactionDto(
            String hash,
            int blockHeight,
            LocalDateTime time,
            long fees,
            List<InputDto> inputs,
            List<OutputDto> outputs
    ) {
        super(hash, blockHeight, time, fees, inputs, outputs);
    }

    static class Deserializer extends TransactionDtoDeserializer<BtcComTransactionDto> {
        Deserializer() {
            super(
                    "hash",
                    "block_height",
                    "fee",
                    "block_time",
                    new BtcComInputOutputDtoDeserializer()
            );
        }

        @Override
        protected Set<JsonNode> getTransactionNodes(JsonNode rootNode) {
            return Set.of(rootNode.get("data"));
        }

        @Override
        protected boolean isCoinbaseTransaction(JsonNode transactionNode) {
            return transactionNode.has("is_coinbase")
                   && transactionNode.get("is_coinbase").booleanValue();
        }

        @Override
        protected BtcComTransactionDto createDtoInstance(
                String hash,
                int blockHeight,
                LocalDateTime time,
                long fees,
                List<InputDto> inputs,
                List<OutputDto> outputs
        ) {
            return new BtcComTransactionDto(hash, blockHeight, time, fees, inputs, outputs);
        }

    }

    private static class BtcComInputOutputDtoDeserializer extends MultiAddressInputOutputDtoDeserializer {
        public BtcComInputOutputDtoDeserializer() {
            super(
                    "prev_value",
                    "value",
                    "prev_addresses",
                    "addresses"
            );
        }

        @Override
        protected boolean isUnsupported(JsonNode inputOutputNode, String address) {
            return !inputOutputNode.has("type") && !inputOutputNode.has("prev_type");
        }

        @Override
        protected int getExpectedNumberOfInputs(JsonNode transactionNode) {
            return transactionNode.get("inputs_count").intValue();
        }

        @Override
        protected int getExpectedNumberOfOutputs(JsonNode transactionNode) {
            return transactionNode.get("outputs_count").intValue();
        }
    }
}

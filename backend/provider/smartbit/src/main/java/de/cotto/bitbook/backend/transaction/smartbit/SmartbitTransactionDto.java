package de.cotto.bitbook.backend.transaction.smartbit;

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

@JsonDeserialize(using = SmartbitTransactionDto.Deserializer.class)
public class SmartbitTransactionDto extends TransactionDto {
    public SmartbitTransactionDto(
            String hash,
            int blockHeight,
            LocalDateTime time,
            long fees,
            List<InputDto> inputs,
            List<OutputDto> outputs
    ) {
        super(hash, blockHeight, time, fees, inputs, outputs);
    }

    static class Deserializer extends TransactionDtoDeserializer<SmartbitTransactionDto> {
        Deserializer() {
            super(
                    "hash",
                    "block",
                    "fee_int",
                    "time",
                    new SmartbitInputOutputDtoDeserializer()
            );
        }

        @Override
        protected Set<JsonNode> getTransactionNodes(JsonNode rootNode) {
            return Set.of(rootNode.get("transaction"));
        }

        @Override
        protected boolean isCoinbaseTransaction(JsonNode transactionNode) {
            return transactionNode.has("coinbase") && transactionNode.get("coinbase").booleanValue();
        }

        @Override
        protected SmartbitTransactionDto createDtoInstance(
                String hash,
                int blockHeight,
                LocalDateTime time,
                long fees,
                List<InputDto> inputs,
                List<OutputDto> outputs
        ) {
            return new SmartbitTransactionDto(hash, blockHeight, time, fees, inputs, outputs);
        }

        private static class SmartbitInputOutputDtoDeserializer
                extends MultiAddressInputOutputDtoDeserializer {
            public SmartbitInputOutputDtoDeserializer() {
                super("value_int", "value_int", "addresses", "addresses");
            }

            @Override
            protected boolean isUnsupported(JsonNode inputOutputNode, String address) {
                JsonNode type = inputOutputNode.get("type");
                return type != null && type.textValue().equals("multisig");
            }
        }
    }
}
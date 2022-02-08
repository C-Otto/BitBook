package de.cotto.bitbook.backend.transaction.blockcypher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.deserialization.InputDto;
import de.cotto.bitbook.backend.transaction.deserialization.MultiAddressInputOutputDtoDeserializer;
import de.cotto.bitbook.backend.transaction.deserialization.OutputDto;
import de.cotto.bitbook.backend.transaction.deserialization.TransactionDto;
import de.cotto.bitbook.backend.transaction.deserialization.TransactionDtoDeserializer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

@JsonDeserialize(using = BlockcypherTransactionDto.Deserializer.class)
public class BlockcypherTransactionDto extends TransactionDto {
    public BlockcypherTransactionDto(
            TransactionHash hash,
            int blockHeight,
            LocalDateTime time,
            long fees,
            List<InputDto> inputs,
            List<OutputDto> outputs
    ) {
        super(hash, blockHeight, time, fees, inputs, outputs);
    }

    public static class Deserializer
            extends TransactionDtoDeserializer<BlockcypherTransactionDto> {

        public static final String HASH_PROPERTY = "hash";
        public static final String BLOCK_HEIGHT_PROPERTY = "block_height";
        public static final String FEES_PROPERTY = "fees";
        public static final String TIME_PROPERTY = "received";

        protected Deserializer() {
            super(HASH_PROPERTY, BLOCK_HEIGHT_PROPERTY, FEES_PROPERTY, TIME_PROPERTY,
                    new BlockcypherInputOutputDtoDeserializer());
        }

        @Override
        protected boolean isUnsupported(JsonNode rootNode) {
            return rootNode.has("next_outputs") || rootNode.has("next_inputs");
        }

        @Override
        protected boolean isCoinbaseTransaction(JsonNode transactionNode) {
            JsonNode inputs = transactionNode.get("inputs");
            if (inputs.isEmpty()) {
                return false;
            }
            JsonNode firstInput = inputs.get(0);
            boolean outputIndexMatches = firstInput.has("output_index")
                                         && firstInput.get("output_index").intValue() == -1;
            boolean scriptTypeMatches = firstInput.has("script_type")
                                        && "empty".equals(firstInput.get("script_type").textValue());
            return outputIndexMatches && scriptTypeMatches;
        }

        @Override
        protected BlockcypherTransactionDto createDtoInstance(
                TransactionHash hash,
                int blockHeight,
                LocalDateTime time,
                long fees,
                List<InputDto> inputs,
                List<OutputDto> outputs
        ) {
            return new BlockcypherTransactionDto(hash, blockHeight, time, fees, inputs, outputs);
        }

        @Override
        protected LocalDateTime parseTime(String time) {
            Instant instant = Instant.from(ISO_INSTANT.parse(time));
            return LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
        }
    }

    private static class BlockcypherInputOutputDtoDeserializer
            extends MultiAddressInputOutputDtoDeserializer {
        public BlockcypherInputOutputDtoDeserializer() {
            super("output_value", "value", "addresses", "addresses");
        }

        @Override
        protected boolean isUnsupported(JsonNode inputOutputNode, String address) {
            return address.startsWith("4");
        }
    }
}

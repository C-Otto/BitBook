package de.cotto.bitbook.backend.transaction.blockchaininfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.deserialization.DefaultInputOutputDtoDeserializer;
import de.cotto.bitbook.backend.transaction.deserialization.InputDto;
import de.cotto.bitbook.backend.transaction.deserialization.OutputDto;
import de.cotto.bitbook.backend.transaction.deserialization.TransactionDto;
import de.cotto.bitbook.backend.transaction.deserialization.TransactionDtoDeserializer;

import java.time.LocalDateTime;
import java.util.List;

@JsonDeserialize(using = BlockchainInfoTransactionDto.Deserializer.class)
public class BlockchainInfoTransactionDto extends TransactionDto {
    public BlockchainInfoTransactionDto(
            TransactionHash hash,
            int blockHeight,
            LocalDateTime time,
            long fees,
            List<InputDto> inputs,
            List<OutputDto> outputs
    ) {
        super(hash, blockHeight, time, fees, inputs, outputs);
    }

    static class Deserializer extends TransactionDtoDeserializer<BlockchainInfoTransactionDto> {
        Deserializer() {
            super(
                    "hash",
                    "block_height",
                    "fee",
                    "time",
                    new DefaultInputOutputDtoDeserializer(
                            List.of("inputs", "prev_out"),
                            List.of("out"),
                            "value",
                            "value",
                            "addr"
                    )
            );
        }

        @Override
        protected boolean isCoinbaseTransaction(JsonNode transactionNode) {
            JsonNode inputs = transactionNode.get("inputs");
            if (inputs.isEmpty()) {
                return false;
            }
            JsonNode firstInput = inputs.get(0);
            if (firstInput.has("index") && firstInput.has("prev_out")) {
                boolean witnessMatches = firstInput.get("index").intValue() == 0;
                boolean prevOutMatches = firstInput.get("prev_out").isNull();
                return witnessMatches && prevOutMatches;
            }
            return false;
        }

        @Override
        protected BlockchainInfoTransactionDto createDtoInstance(
                TransactionHash hash,
                int blockHeight,
                LocalDateTime time,
                long fees,
                List<InputDto> inputs,
                List<OutputDto> outputs
        ) {
            return new BlockchainInfoTransactionDto(hash, blockHeight, time, fees, inputs, outputs);
        }
    }
}

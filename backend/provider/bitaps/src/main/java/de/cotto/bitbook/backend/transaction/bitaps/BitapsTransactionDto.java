package de.cotto.bitbook.backend.transaction.bitaps;

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
import java.util.Set;

@JsonDeserialize(using = BitapsTransactionDto.Deserializer.class)
public class BitapsTransactionDto extends TransactionDto {
    public BitapsTransactionDto(
            TransactionHash hash,
            int blockHeight,
            LocalDateTime time,
            long fees,
            List<InputDto> inputs,
            List<OutputDto> outputs
    ) {
        super(hash, blockHeight, time, fees, inputs, outputs);
    }

    static class Deserializer extends TransactionDtoDeserializer<BitapsTransactionDto> {
        Deserializer() {
            super(
                    "txId",
                    "blockHeight",
                    "fee",
                    "time",
                    new DefaultInputOutputDtoDeserializer(
                            List.of("vIn", "*"),
                            List.of("vOut", "*"),
                            "amount",
                            "value",
                            "address"
                    )
            );
        }

        @Override
        protected Set<JsonNode> getTransactionNodes(JsonNode rootNode) {
            return Set.of(rootNode.get("data"));
        }

        @Override
        @SuppressWarnings("PMD.AvoidBranchingStatementAsLastInLoop")
        protected boolean isCoinbaseTransaction(JsonNode transactionNode) {
            JsonNode inputNode = transactionNode.get("vIn").iterator().next();
            return inputNode.has("txId")
                    && "0000000000000000000000000000000000000000000000000000000000000000"
                    .equals(inputNode.get("txId").textValue());
        }

        @Override
        protected BitapsTransactionDto createDtoInstance(
                TransactionHash hash,
                int blockHeight,
                LocalDateTime time,
                long fees,
                List<InputDto> inputs,
                List<OutputDto> outputs
        ) {
            return new BitapsTransactionDto(hash, blockHeight, time, fees, inputs, outputs);
        }
    }
}

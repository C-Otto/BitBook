package de.cotto.bitbook.backend.transaction.blockchair;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.deserialization.DefaultInputOutputDtoDeserializer;
import de.cotto.bitbook.backend.transaction.deserialization.InputDto;
import de.cotto.bitbook.backend.transaction.deserialization.OutputDto;
import de.cotto.bitbook.backend.transaction.deserialization.TransactionDto;
import de.cotto.bitbook.backend.transaction.deserialization.TransactionDtoDeserializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.StreamSupport;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.util.stream.Collectors.toSet;

@JsonDeserialize(using = BlockchairTransactionDto.Deserializer.class)
public class BlockchairTransactionDto extends TransactionDto {
    public BlockchairTransactionDto(
            TransactionHash hash,
            int blockHeight,
            LocalDateTime time,
            long fees,
            List<InputDto> inputs,
            List<OutputDto> outputs
    ) {
        super(hash, blockHeight, time, fees, inputs, outputs);
    }

    static class Deserializer extends TransactionDtoDeserializer<BlockchairTransactionDto> {
        Deserializer() {
            super(
                    "hash",
                    "block_id",
                    "fee",
                    "time",
                    new DefaultInputOutputDtoDeserializer(
                            List.of("inputs"),
                            List.of("outputs"),
                            "value",
                            "value",
                            "recipient"
                    )
            );
        }

        @Override
        protected Set<JsonNode> getTransactionNodes(JsonNode rootNode) {
            return StreamSupport.stream(rootNode.get("data").spliterator(), false).collect(toSet());
        }

        @Override
        protected JsonNode getTransactionDetailsNode(JsonNode transactionNode) {
            return transactionNode.get("transaction");
        }

        @Override
        protected boolean isCoinbaseTransaction(JsonNode transactionNode) {
            JsonNode transactionDetailsNode = transactionNode.get("transaction");
            return transactionDetailsNode.has("is_coinbase")
                   && transactionDetailsNode.get("is_coinbase").booleanValue();
        }

        @Override
        protected BlockchairTransactionDto createDtoInstance(
                TransactionHash hash,
                int blockHeight,
                LocalDateTime time,
                long fees,
                List<InputDto> inputs,
                List<OutputDto> outputs
        ) {
            return new BlockchairTransactionDto(hash, blockHeight, time, fees, inputs, outputs);
        }

        @Override
        protected LocalDateTime parseTime(String time) {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .append(ISO_LOCAL_DATE)
                    .appendLiteral(' ')
                    .append(ISO_LOCAL_TIME)
                    .toFormatter(Locale.US);
            return LocalDateTime.from(formatter.parse(time));
        }
    }
}

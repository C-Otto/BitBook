package de.cotto.bitbook.backend.transaction.deserialization;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;
import java.util.List;

@JsonDeserialize(using = TestableTransactionDtoDeserializer.class)
class TestableTransactionDto extends TransactionDto {
    public TestableTransactionDto(
            String hash,
            int blockHeight,
            LocalDateTime time,
            long fees,
            List<InputDto> inputs,
            List<OutputDto> outputs
    ) {
        super(hash, blockHeight, time, fees, inputs, outputs);
    }
}

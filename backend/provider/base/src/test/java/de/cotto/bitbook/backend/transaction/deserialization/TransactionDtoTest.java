package de.cotto.bitbook.backend.transaction.deserialization;

import de.cotto.bitbook.backend.model.Transaction;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.model.TransactionFixtures.FEES;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.deserialization.InputDtoFixtures.INPUT_DTO_1;
import static de.cotto.bitbook.backend.transaction.deserialization.InputDtoFixtures.INPUT_DTO_2;
import static de.cotto.bitbook.backend.transaction.deserialization.OutputDtoFixtures.OUTPUT_DTO_1;
import static de.cotto.bitbook.backend.transaction.deserialization.OutputDtoFixtures.OUTPUT_DTO_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class TransactionDtoTest {
    @Test
    void toModel_unknown() {
        TestableTransactionDto transactionDto = new TestableTransactionDto(
                "", 0, LocalDateTime.MIN, 0, List.of(), List.of()
        );
        Transaction model = transactionDto.toModel();
        assertThat(model).isEqualTo(Transaction.UNKNOWN);
    }

    @Test
    void toModel_coinbase() {
        TestableTransactionDto transactionDto = new TestableTransactionDto(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                FEES.getSatoshis(),
                List.of(InputDto.COINBASE),
                List.of(OUTPUT_DTO_1, OUTPUT_DTO_2)
        );
        Transaction model = transactionDto.toModel();
        assertThat(model.getOutputs()).hasSize(2);
    }

    @Test
    void toModel_coinbase_input_and_something_else() {
        TestableTransactionDto transactionDto = new TestableTransactionDto(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                FEES.getSatoshis(),
                List.of(InputDto.COINBASE, INPUT_DTO_2),
                List.of(OUTPUT_DTO_1, OUTPUT_DTO_2)
        );
        assertThatNullPointerException().isThrownBy(transactionDto::toModel);
    }

    @Test
    void toModel_single_input() {
        TestableTransactionDto transactionDto = new TestableTransactionDto(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                21_515L,
                List.of(INPUT_DTO_1),
                List.of(OUTPUT_DTO_2)
        );
        Transaction model = transactionDto.toModel();
        assertThat(model.getInputs()).hasSize(1);
    }

    @Test
    void toModel() {
        TestableTransactionDto transactionDto = new TestableTransactionDto(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                FEES.getSatoshis(),
                List.of(INPUT_DTO_1, INPUT_DTO_2),
                List.of(OUTPUT_DTO_1, OUTPUT_DTO_2)
        );
        Transaction model = transactionDto.toModel();
        assertThat(model).isEqualTo(TRANSACTION);
    }
}
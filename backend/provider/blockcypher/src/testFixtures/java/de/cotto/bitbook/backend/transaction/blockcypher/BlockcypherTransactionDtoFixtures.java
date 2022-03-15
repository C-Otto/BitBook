package de.cotto.bitbook.backend.transaction.blockcypher;

import java.util.List;

import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.model.TransactionFixtures.FEES;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.deserialization.InputDtoFixtures.INPUT_DTO_1;
import static de.cotto.bitbook.backend.transaction.deserialization.InputDtoFixtures.INPUT_DTO_2;
import static de.cotto.bitbook.backend.transaction.deserialization.OutputDtoFixtures.OUTPUT_DTO_1;
import static de.cotto.bitbook.backend.transaction.deserialization.OutputDtoFixtures.OUTPUT_DTO_2;

public class BlockcypherTransactionDtoFixtures {
    public static final BlockcypherTransactionDto BLOCKCYPHER_TRANSACTION;

    static {
        BLOCKCYPHER_TRANSACTION = new BlockcypherTransactionDto(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                FEES.satoshis(),
                List.of(INPUT_DTO_1, INPUT_DTO_2),
                List.of(OUTPUT_DTO_1, OUTPUT_DTO_2)
        );
    }
}

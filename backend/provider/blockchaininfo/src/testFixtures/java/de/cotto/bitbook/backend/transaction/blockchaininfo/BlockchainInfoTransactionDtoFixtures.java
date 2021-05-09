package de.cotto.bitbook.backend.transaction.blockchaininfo;

import java.util.List;

import static de.cotto.bitbook.backend.transaction.deserialization.InputDtoFixtures.INPUT_DTO_1;
import static de.cotto.bitbook.backend.transaction.deserialization.InputDtoFixtures.INPUT_DTO_2;
import static de.cotto.bitbook.backend.transaction.deserialization.OutputDtoFixtures.OUTPUT_DTO_1;
import static de.cotto.bitbook.backend.transaction.deserialization.OutputDtoFixtures.OUTPUT_DTO_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.FEES;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;

public class BlockchainInfoTransactionDtoFixtures {
    public static final BlockchainInfoTransactionDto BLOCKCHAIN_INFO_TRANSACTION =
            new BlockchainInfoTransactionDto(
                    TRANSACTION_HASH,
                    BLOCK_HEIGHT,
                    DATE_TIME,
                    FEES.getSatoshis(),
                    List.of(INPUT_DTO_1, INPUT_DTO_2),
                    List.of(OUTPUT_DTO_1, OUTPUT_DTO_2)
            );

}

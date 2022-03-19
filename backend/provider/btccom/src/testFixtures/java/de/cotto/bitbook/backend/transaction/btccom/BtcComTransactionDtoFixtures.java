package de.cotto.bitbook.backend.transaction.btccom;

import java.util.List;

import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.model.TransactionFixtures.FEES;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.deserialization.InputDtoFixtures.INPUT_DTO_1;
import static de.cotto.bitbook.backend.transaction.deserialization.InputDtoFixtures.INPUT_DTO_2;
import static de.cotto.bitbook.backend.transaction.deserialization.OutputDtoFixtures.OUTPUT_DTO_1;
import static de.cotto.bitbook.backend.transaction.deserialization.OutputDtoFixtures.OUTPUT_DTO_2;

public class BtcComTransactionDtoFixtures {
    public static final BtcComTransactionDto BTCCOM_TRANSACTION;

    static {
        BTCCOM_TRANSACTION = new BtcComTransactionDto(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                FEES.satoshis(),
                List.of(INPUT_DTO_1, INPUT_DTO_2),
                List.of(OUTPUT_DTO_1, OUTPUT_DTO_2)
        );
    }
}

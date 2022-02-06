package de.cotto.bitbook.backend.transaction.persistence;

import java.time.ZoneOffset;
import java.util.List;

import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.model.TransactionFixtures.FEES;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.persistence.InputJpaDtoFixtures.INPUT_JPA_DTO_1;
import static de.cotto.bitbook.backend.transaction.persistence.InputJpaDtoFixtures.INPUT_JPA_DTO_2;
import static de.cotto.bitbook.backend.transaction.persistence.OutputJpaDtoFixtures.OUTPUT_JPA_DTO_1;
import static de.cotto.bitbook.backend.transaction.persistence.OutputJpaDtoFixtures.OUTPUT_JPA_DTO_2;

public class TransactionJpaDtoFixtures {
    public static final TransactionJpaDto TRANSACTION_JPA_DTO;

    static {
        TRANSACTION_JPA_DTO = new TransactionJpaDto();
        TRANSACTION_JPA_DTO.setHash(TRANSACTION_HASH);
        TRANSACTION_JPA_DTO.setBlockHeight(BLOCK_HEIGHT);
        TRANSACTION_JPA_DTO.setTime(DATE_TIME.toEpochSecond(ZoneOffset.UTC));
        TRANSACTION_JPA_DTO.setFees(FEES.getSatoshis());
        TRANSACTION_JPA_DTO.setInputs(List.of(INPUT_JPA_DTO_1, INPUT_JPA_DTO_2));
        TRANSACTION_JPA_DTO.setOutputs(List.of(OUTPUT_JPA_DTO_1, OUTPUT_JPA_DTO_2));
    }
}

package de.cotto.bitbook.backend.transaction.persistence;

import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Output;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.FEES;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.persistence.TransactionJpaDtoFixtures.TRANSACTION_JPA_DTO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TransactionJpaDtoTest {

    @Test
    void toModel_nullHash() {
        TransactionJpaDto dto = new TransactionJpaDto();
        Transaction model = dto.toModel();
        assertThat(model).isEqualTo(Transaction.UNKNOWN);
    }

    @Test
    void toModel() {
        Transaction model = TRANSACTION_JPA_DTO.toModel();
        assertThat(model).isEqualTo(TRANSACTION);
    }

    @Test
    void fromModel() {
        TransactionJpaDto fromModel = TransactionJpaDto.fromModel(TRANSACTION);
        assertThat(fromModel).usingRecursiveComparison().isEqualTo(TRANSACTION_JPA_DTO);
    }

    @Test
    void coinbase_transaction_can_be_recreated() {
        Transaction coinbaseTransaction = Transaction.forCoinbase(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                FEES,
                List.of(OUTPUT_1, OUTPUT_2, new Output(Coins.NONE, "xxx"))
        );
        assertThat(TransactionJpaDto.fromModel(coinbaseTransaction).toModel()).isEqualTo(coinbaseTransaction);
    }
}
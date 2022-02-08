package de.cotto.bitbook.backend.persistence;

import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.model.TransactionWithDescription;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionWithDescriptionJpaDtoTest {

    private static final TransactionHash SOME_HASH = new TransactionHash("x");

    @Test
    void getTransactionHash() {
        assertThat(new TransactionWithDescriptionJpaDto("x", "y").getTransactionHash()).isEqualTo(SOME_HASH);
    }

    @Test
    void getDescription() {
        assertThat(new TransactionWithDescriptionJpaDto("x", "y").getDescription()).isEqualTo("y");
    }

    @Test
    void toModel() {
        assertThat(new TransactionWithDescriptionJpaDto("x", "y").toModel())
                .isEqualTo(new TransactionWithDescription(SOME_HASH, "y"));
    }

    @Test
    void toModel_without_description() {
        TransactionWithDescriptionJpaDto dto = new TransactionWithDescriptionJpaDto();
        ReflectionTestUtils.setField(dto, "transactionHash", "x");
        assertThat(dto.toModel())
                .isEqualTo(new TransactionWithDescription(SOME_HASH, ""));
    }

    @Test
    void fromModel() {
        assertThat(TransactionWithDescriptionJpaDto.fromModel(new TransactionWithDescription(SOME_HASH, "y")))
                .usingRecursiveComparison()
                .isEqualTo(new TransactionWithDescriptionJpaDto("x", "y"));
    }
}
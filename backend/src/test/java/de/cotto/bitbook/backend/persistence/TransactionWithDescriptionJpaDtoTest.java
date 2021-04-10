package de.cotto.bitbook.backend.persistence;

import de.cotto.bitbook.backend.model.TransactionWithDescription;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionWithDescriptionJpaDtoTest {
    @Test
    void getTransactionHash() {
        assertThat(new TransactionWithDescriptionJpaDto("x", "y").getTransactionHash()).isEqualTo("x");
    }

    @Test
    void getDescription() {
        assertThat(new TransactionWithDescriptionJpaDto("x", "y").getDescription()).isEqualTo("y");
    }

    @Test
    void toModel() {
        assertThat(new TransactionWithDescriptionJpaDto("x", "y").toModel())
                .isEqualTo(new TransactionWithDescription("x", "y"));
    }

    @Test
    void toModel_without_description() {
        TransactionWithDescriptionJpaDto dto = new TransactionWithDescriptionJpaDto();
        ReflectionTestUtils.setField(dto, "transactionHash", "x");
        assertThat(dto.toModel())
                .isEqualTo(new TransactionWithDescription("x", ""));
    }

    @Test
    void fromModel() {
        assertThat(TransactionWithDescriptionJpaDto.fromModel(new TransactionWithDescription("x", "y")))
                .usingRecursiveComparison()
                .isEqualTo(new TransactionWithDescriptionJpaDto("x", "y"));
    }
}
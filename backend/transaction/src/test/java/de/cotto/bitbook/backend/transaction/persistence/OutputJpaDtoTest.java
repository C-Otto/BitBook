package de.cotto.bitbook.backend.transaction.persistence;

import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.persistence.OutputJpaDtoFixtures.OUTPUT_JPA_DTO_1;
import static org.assertj.core.api.Assertions.assertThat;

class OutputJpaDtoTest {
    @Test
    void toModel() {
        assertThat(OUTPUT_JPA_DTO_1.toModel()).isEqualTo(OUTPUT_1);
    }

    @Test
    void fromModel() {
        assertThat(OutputJpaDto.fromModel(OUTPUT_1)).usingRecursiveComparison().isEqualTo(OUTPUT_JPA_DTO_1);
    }

    @Test
    void getAddress() {
        assertThat(OUTPUT_JPA_DTO_1.getAddress()).isEqualTo(OUTPUT_ADDRESS_1);
    }
}
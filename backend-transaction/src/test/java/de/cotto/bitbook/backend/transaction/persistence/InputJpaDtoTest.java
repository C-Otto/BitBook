package de.cotto.bitbook.backend.transaction.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_1;
import static de.cotto.bitbook.backend.transaction.persistence.InputJpaDtoFixtures.INPUT_JPA_DTO_1;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InputJpaDtoTest {
    @Test
    void toModel() {
        assertThat(INPUT_JPA_DTO_1.toModel()).isEqualTo(INPUT_1);
    }

    @Test
    void fromModel() {
        assertThat(InputJpaDto.fromModel(INPUT_1)).usingRecursiveComparison().isEqualTo(INPUT_JPA_DTO_1);
    }
}
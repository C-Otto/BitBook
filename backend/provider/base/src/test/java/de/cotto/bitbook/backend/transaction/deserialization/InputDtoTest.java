package de.cotto.bitbook.backend.transaction.deserialization;

import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.transaction.deserialization.InputDtoFixtures.INPUT_DTO_1;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_1;
import static org.assertj.core.api.Assertions.assertThat;

class InputDtoTest {
    @Test
    void toModel() {
        assertThat(INPUT_DTO_1.toModel()).isEqualTo(INPUT_1);
    }
}
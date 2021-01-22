package de.cotto.bitbook.backend.transaction.deserialization;

import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.transaction.deserialization.OutputDtoFixtures.OUTPUT_DTO_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_1;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class OutputDtoTest {
    @Test
    void toModel() {
        assertThat(OUTPUT_DTO_1.toModel()).isEqualTo(OUTPUT_1);
    }
}
package de.cotto.bitbook.backend.transaction.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_VALUE_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class OutputTest {
    @Test
    void empty() {
        assertThat(Output.EMPTY).isEqualTo(new Output(Coins.NONE, ""));
    }

    @Test
    void noNegativeAmount() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
            assertThat(Output.EMPTY).isEqualTo(new Output(Coins.ofSatoshis(-1), ""))
        );
    }

    @Test
    void testToString() {
        assertThat(OUTPUT_1)
                .hasToString("Output{value=%s, address='%s'}".formatted(OUTPUT_VALUE_1, OUTPUT_ADDRESS_1));
    }

    @Test
    void testEquals() {
        EqualsVerifier.configure().suppress(Warning.NULL_FIELDS).forClass(Output.class).usingGetClass().verify();
    }

    @Test
    void getTargetAddress() {
        assertThat(OUTPUT_1.getAddress()).isEqualTo(OUTPUT_ADDRESS_1);
    }

    @Test
    void getValue() {
        assertThat(OUTPUT_1.getValue()).isEqualTo(OUTPUT_VALUE_1);
    }
}

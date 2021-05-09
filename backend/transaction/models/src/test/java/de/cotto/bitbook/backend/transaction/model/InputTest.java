package de.cotto.bitbook.backend.transaction.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_1;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_VALUE_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class InputTest {
    @Test
    void empty() {
        assertThat(Input.EMPTY).isEqualTo(new Input(Coins.NONE, ""));
    }

    @Test
    void noNegativeAmount() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
            assertThat(Input.EMPTY).isEqualTo(new Input(Coins.ofSatoshis(-1), ""))
        );
    }

    @Test
    void testToString() {
        assertThat(INPUT_1)
                .hasToString("Input{value=%s, address='%s'}".formatted(INPUT_VALUE_1, INPUT_ADDRESS_1));
    }

    @Test
    void testEquals() {
        EqualsVerifier.configure().suppress(Warning.NULL_FIELDS).forClass(Input.class).usingGetClass().verify();
    }

    @Test
    void getSourceAddress() {
        assertThat(INPUT_1.getAddress()).isEqualTo(INPUT_ADDRESS_1);
    }

    @Test
    void getValue() {
        assertThat(INPUT_1.getValue()).isEqualTo(INPUT_VALUE_1);
    }
}

package de.cotto.bitbook.backend.price.persistence;

import de.cotto.bitbook.backend.price.model.PriceContext;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static de.cotto.bitbook.backend.model.Chain.BTC;
import static org.assertj.core.api.Assertions.assertThat;

class PriceWithContextIdTest {

    public static final LocalDate DATE = LocalDate.now(ZoneOffset.UTC);

    @Test
    void fromModel() {
        assertThat(PriceWithContextId.fromModel(new PriceContext(DATE, BTC)))
                .isEqualTo(new PriceWithContextId(DATE, "BTC"));
    }

    @Test
    void testEquals() {
        EqualsVerifier.simple().forClass(PriceWithContextId.class).verify();
    }
}
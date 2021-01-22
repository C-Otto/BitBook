package de.cotto.bitbook.backend.persistence;

import de.cotto.bitbook.backend.model.AddressWithDescription;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class AddressWithDescriptionJpaDtoTest {
    @Test
    void getAddress() {
        assertThat(new AddressWithDescriptionJpaDto("x", "y").getAddress()).isEqualTo("x");
    }

    @Test
    void getDescription() {
        assertThat(new AddressWithDescriptionJpaDto("x", "y").getDescription()).isEqualTo("y");
    }

    @Test
    void toModel() {
        assertThat(new AddressWithDescriptionJpaDto("x", "y").toModel())
                .isEqualTo(new AddressWithDescription("x", "y"));
    }

    @Test
    void toModel_without_description() {
        AddressWithDescriptionJpaDto dto = new AddressWithDescriptionJpaDto();
        ReflectionTestUtils.setField(dto, "address", "x");
        assertThat(dto.toModel())
                .isEqualTo(new AddressWithDescription("x", ""));
    }

    @Test
    void fromModel() {
        assertThat(AddressWithDescriptionJpaDto.fromModel(new AddressWithDescription("x", "y")))
                .usingRecursiveComparison()
                .isEqualTo(new AddressWithDescriptionJpaDto("x", "y"));
    }
}
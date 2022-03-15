package de.cotto.bitbook.backend.transaction.persistence;

import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_VALUE_1;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_VALUE_2;

public class InputJpaDtoFixtures {
    public static final InputJpaDto INPUT_JPA_DTO_1;
    public static final InputJpaDto INPUT_JPA_DTO_2;

    static {
        INPUT_JPA_DTO_1 = new InputJpaDto();
        INPUT_JPA_DTO_1.setValue(INPUT_VALUE_1.satoshis());
        INPUT_JPA_DTO_1.setSourceAddress(INPUT_ADDRESS_1.toString());

        INPUT_JPA_DTO_2 = new InputJpaDto();
        INPUT_JPA_DTO_2.setValue(INPUT_VALUE_2.satoshis());
        INPUT_JPA_DTO_2.setSourceAddress(INPUT_ADDRESS_2.toString());
    }
}

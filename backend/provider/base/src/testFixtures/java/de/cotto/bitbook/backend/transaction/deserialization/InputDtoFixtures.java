package de.cotto.bitbook.backend.transaction.deserialization;

import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_VALUE_1;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_VALUE_2;

public class InputDtoFixtures {
    public static final InputDto INPUT_DTO_1;
    public static final InputDto INPUT_DTO_2;

    static {
        INPUT_DTO_1 = new InputDto();
        INPUT_DTO_1.setValue(INPUT_VALUE_1);
        INPUT_DTO_1.setAddress(INPUT_ADDRESS_1);

        INPUT_DTO_2 = new InputDto();
        INPUT_DTO_2.setValue(INPUT_VALUE_2);
        INPUT_DTO_2.setAddress(INPUT_ADDRESS_2);
    }
}

package de.cotto.bitbook.backend.transaction.deserialization;

import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_VALUE_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_VALUE_2;

public class OutputDtoFixtures {
    public static final OutputDto OUTPUT_DTO_1;
    public static final OutputDto OUTPUT_DTO_2;

    static {
        OUTPUT_DTO_1 = new OutputDto();
        OUTPUT_DTO_1.setValue(OUTPUT_VALUE_1);
        OUTPUT_DTO_1.setAddress(OUTPUT_ADDRESS_1);

        OUTPUT_DTO_2 = new OutputDto();
        OUTPUT_DTO_2.setValue(OUTPUT_VALUE_2);
        OUTPUT_DTO_2.setAddress(OUTPUT_ADDRESS_2);
    }
}

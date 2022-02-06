package de.cotto.bitbook.backend.transaction.persistence;

import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_VALUE_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_VALUE_2;

public class OutputJpaDtoFixtures {
    public static final OutputJpaDto OUTPUT_JPA_DTO_1;
    public static final OutputJpaDto OUTPUT_JPA_DTO_2;

    static {
        OUTPUT_JPA_DTO_1 = new OutputJpaDto();
        OUTPUT_JPA_DTO_1.setValue(OUTPUT_VALUE_1.getSatoshis());
        OUTPUT_JPA_DTO_1.setTargetAddress(OUTPUT_ADDRESS_1);

        OUTPUT_JPA_DTO_2 = new OutputJpaDto();
        OUTPUT_JPA_DTO_2.setValue(OUTPUT_VALUE_2.getSatoshis());
        OUTPUT_JPA_DTO_2.setTargetAddress(OUTPUT_ADDRESS_2);
    }
}

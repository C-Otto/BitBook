package de.cotto.bitbook.backend.transaction.deserialization;

import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Output;

public class OutputDto extends InputOutputDto {
    public OutputDto() {
        super();
    }

    public OutputDto(Coins value, String address) {
        super(value, address);
    }

    @Override
    public Output toModel() {
        return new Output(getValue(), getAddress());
    }
}

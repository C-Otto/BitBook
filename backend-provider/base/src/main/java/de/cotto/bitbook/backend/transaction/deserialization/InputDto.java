package de.cotto.bitbook.backend.transaction.deserialization;

import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Input;

public class InputDto extends InputOutputDto {
    public static final InputDto COINBASE = new InputDto();

    public InputDto() {
        super();
    }

    public InputDto(Coins value, String address) {
        super(value, address);
    }

    @Override
    public Input toModel() {
        return new Input(getValue(), getAddress());
    }
}

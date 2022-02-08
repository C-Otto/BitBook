package de.cotto.bitbook.backend.model;

public class Input extends InputOutput {
    public static final Input EMPTY = new Input(Coins.NONE, Address.NONE);

    public Input(Coins value, Address address) {
        super(value, address);
    }
}

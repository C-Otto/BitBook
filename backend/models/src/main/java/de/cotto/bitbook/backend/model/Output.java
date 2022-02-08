package de.cotto.bitbook.backend.model;

public class Output extends InputOutput {
    public static final Output EMPTY = new Output(Coins.NONE, Address.NONE);

    public Output(Coins value, Address targetAddress) {
        super(value, targetAddress);
    }
}

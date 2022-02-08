package de.cotto.bitbook.backend.model;

public class OutputFixtures {
    public static final Coins OUTPUT_VALUE_1 = Coins.ofSatoshis(Integer.MAX_VALUE + 1L);
    public static final Coins OUTPUT_VALUE_2 = Coins.ofSatoshis(1_234);
    public static final Address OUTPUT_ADDRESS_1 =
            new Address("bc1qt9n59nfqcw2la4ms7zsphqllm5789syhrgcupw");
    public static final Address OUTPUT_ADDRESS_2 =
            new Address("bc1qc7slrfxkknqcq2jevvvkdgvrt8080852dfjewde450xdlk4ugp7szw5tk9");
    public static final Output OUTPUT_1 = new Output(OUTPUT_VALUE_1, OUTPUT_ADDRESS_1);
    public static final Output OUTPUT_2 = new Output(OUTPUT_VALUE_2, OUTPUT_ADDRESS_2);
}

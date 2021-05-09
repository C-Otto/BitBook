package de.cotto.bitbook.backend.transaction.model;

public class InputFixtures {
    public static final Coins INPUT_VALUE_1 = Coins.ofSatoshis(22_749);
    public static final Coins INPUT_VALUE_2 = Coins.ofSatoshis(Integer.MAX_VALUE - 1L);
    public static final String INPUT_ADDRESS_1 = "bc1xxxn59nfqcw2la4ms7zsphqllm5789syhrgcupw";
    public static final String INPUT_ADDRESS_2 = "bc1yyyn59nfqcw2la4ms7zsphqllm5789syhrgcupw";
    public static final Input INPUT_1 = new Input(INPUT_VALUE_1, INPUT_ADDRESS_1);
    public static final Input INPUT_2 = new Input(INPUT_VALUE_2, INPUT_ADDRESS_2);
}

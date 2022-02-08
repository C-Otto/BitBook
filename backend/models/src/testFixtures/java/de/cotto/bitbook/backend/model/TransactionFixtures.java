package de.cotto.bitbook.backend.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_2;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.TRANSACTION_HASH_4;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_1;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_2;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_2;

public class TransactionFixtures {
    public static final String TRANSACTION_HASH = "c56c2a4ec7099879c2c4da74f4e5105a5a5d0ed94aa7d64518fa7e4256d42d9e";
    public static final String TRANSACTION_HASH_2 = "aad0e9e8f453da1a207600f856325f10b2e1a03c39c308481855925f15ed4cfe";
    public static final int BLOCK_HEIGHT = 601_164;
    public static final LocalDateTime DATE_TIME = LocalDateTime.of(2019, 10, 26, 20, 6, 35);
    public static final long DATE_TIME_EPOCH_SECONDS = DATE_TIME.toEpochSecond(ZoneOffset.UTC);
    public static final Coins FEES = Coins.ofSatoshis(21_513);
    public static final Transaction TRANSACTION = new Transaction(
            TRANSACTION_HASH,
            BLOCK_HEIGHT,
            DATE_TIME,
            FEES,
            List.of(INPUT_1, INPUT_2),
            List.of(OUTPUT_1, OUTPUT_2)
    );

    public static final Transaction TRANSACTION_2 = new Transaction(
            TRANSACTION_HASH_2,
            BLOCK_HEIGHT,
            DATE_TIME.plusDays(12),
            Coins.NONE,
            List.of(new Input(Coins.ofSatoshis(2_147_484_882L), new Address("abc"))),
            List.of(OUTPUT_1, OUTPUT_2)
    );

    public static final Transaction TRANSACTION_3 = new Transaction(
            TRANSACTION_HASH_3,
            BLOCK_HEIGHT,
            DATE_TIME,
            Coins.NONE,
            List.of(INPUT_1, new Input(Coins.ofSatoshis(2_147_460_899L), ADDRESS)),
            List.of(OUTPUT_1)
    );

    public static final Transaction TRANSACTION_4 = new Transaction(
            TRANSACTION_HASH_4,
            BLOCK_HEIGHT,
            DATE_TIME,
            Coins.NONE,
            List.of(new Input(Coins.ofSatoshis(2), ADDRESS_2), INPUT_2),
            List.of(OUTPUT_1)
    );
}

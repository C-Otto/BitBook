package de.cotto.bitbook.lnd.model;

import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Input;
import de.cotto.bitbook.backend.model.Output;
import de.cotto.bitbook.backend.model.Transaction;

import java.util.List;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.DATE_TIME;

public class ChannelFixtures {
    public static final Coins CHANNEL_CAPACITY = Coins.ofSatoshis(555);
    public static final String CHANNEL_ADDRESS = "bc1somechannel";

    public static final Transaction OPENING_TRANSACTION = new Transaction(
            TRANSACTION_HASH_3,
            BLOCK_HEIGHT,
            DATE_TIME,
            Coins.ofSatoshis(45),
            List.of(
                    new Input(Coins.ofSatoshis(550), INPUT_ADDRESS_1),
                    new Input(Coins.ofSatoshis(50), INPUT_ADDRESS_2)
            ),
            List.of(new Output(CHANNEL_CAPACITY, CHANNEL_ADDRESS))
    );
    public static final String REMOTE_PUBKEY = "pubkey";

    public static final Channel CHANNEL = new Channel(false, REMOTE_PUBKEY, OPENING_TRANSACTION, 0);
    public static final Channel CHANNEL_LOCAL = new Channel(true, REMOTE_PUBKEY, OPENING_TRANSACTION, 0);
}

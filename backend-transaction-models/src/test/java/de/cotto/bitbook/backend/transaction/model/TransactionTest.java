package de.cotto.bitbook.backend.transaction.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_1;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_2;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_VALUE_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_2;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_VALUE_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_VALUE_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.FEES;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TransactionTest {

    @Test
    void unknown_transaction_is_equal_to_any_transaction_with_empty_hash() {
        assertThat(Transaction.UNKNOWN).isEqualTo(new Transaction("", BLOCK_HEIGHT));
    }

    @Test
    void isValid_false() {
        assertThat(Transaction.UNKNOWN.isValid()).isFalse();
    }

    @Test
    void isInvalid_true() {
        assertThat(Transaction.UNKNOWN.isInvalid()).isTrue();
    }

    @Test
    void isValid_true() {
        assertThat(TRANSACTION.isValid()).isTrue();
    }

    @Test
    void isInvalid_false() {
        assertThat(TRANSACTION.isInvalid()).isFalse();
    }

    @Test
    void forCoinbase() {
        Transaction coinbaseTransaction = Transaction.forCoinbase(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                FEES,
                List.of(OUTPUT_1, OUTPUT_2, new Output(Coins.NONE, "xxx"))
        );
        assertThat(coinbaseTransaction.getOutputs()).hasSize(2);
    }

    @Test
    void getIncomingToAndOutgoingFromAddress_no_match() {
        assertThat(TRANSACTION.getIncomingToAndOutgoingFromAddress(ADDRESS)).isEmpty();
    }

    @Test
    void getIncomingToAndOutgoingFromAddress_returns_inputs_if_output_funds_address() {
        assertThat(TRANSACTION.getIncomingToAndOutgoingFromAddress(OUTPUT_ADDRESS_1))
                .containsExactly(INPUT_1, INPUT_2);
    }

    @Test
    void getIncomingToAndOutgoingFromAddress_returns_outputs_if_input_takes_from_address() {
        assertThat(TRANSACTION.getIncomingToAndOutgoingFromAddress(INPUT_ADDRESS_2))
                .containsExactly(OUTPUT_1, OUTPUT_2);
    }

    @Test
    void getIncomingCoins() {
        assertThat(TRANSACTION.getIncomingCoins(OUTPUT_ADDRESS_1)).isEqualTo(OUTPUT_VALUE_1);
    }

    @Test
    void getOutgoingCoins() {
        assertThat(TRANSACTION.getOutgoingCoins(INPUT_ADDRESS_1)).isEqualTo(INPUT_VALUE_1);
    }

    @Test
    void getDifferenceForAddress() {
        Coins coins1 = Coins.ofSatoshis(10);
        Coins coins2 = Coins.ofSatoshis(20);
        Coins coins3 = Coins.ofSatoshis(5);
        Coins coins4 = Coins.ofSatoshis(25);
        Transaction transaction = new Transaction(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                Coins.NONE,
                List.of(new Input(coins1, ADDRESS), new Input(coins2, ADDRESS)),
                List.of(new Output(coins3, ADDRESS_2), new Output(coins4, ADDRESS))
        );
        Coins difference = coins4.subtract(coins1).subtract(coins2);
        assertThat(transaction.getDifferenceForAddress(ADDRESS)).isEqualTo(difference);
    }

    @Test
    void getDifferenceForAddress_negative() {
        Coins coinsIn = Coins.ofSatoshis(10);
        Coins coinsOut = Coins.ofSatoshis(5);
        Transaction transaction = new Transaction(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                coinsIn.subtract(coinsOut),
                List.of(new Input(coinsIn, ADDRESS)),
                List.of(new Output(coinsOut, ADDRESS_2))
        );
        Coins difference = Coins.NONE.subtract(coinsIn);
        assertThat(transaction.getDifferenceForAddress(ADDRESS)).isEqualTo(difference);
    }

    @Test
    void getAllAddresses() {
        assertThat(TRANSACTION.getAllAddresses()).containsExactlyInAnyOrder(
                INPUT_ADDRESS_1, INPUT_ADDRESS_2, OUTPUT_ADDRESS_1, OUTPUT_ADDRESS_2
        );
    }

    @Test
    void getAllInputAddresses() {
        assertThat(TRANSACTION.getInputAddresses()).containsExactlyInAnyOrder(
                INPUT_ADDRESS_1, INPUT_ADDRESS_2
        );
    }

    @Test
    void getAllOutputAddresses() {
        assertThat(TRANSACTION.getOutputAddresses()).containsExactlyInAnyOrder(
                OUTPUT_ADDRESS_1, OUTPUT_ADDRESS_2
        );
    }

    @Test
    void getOutputWithValue_found() {
        assertThat(TRANSACTION.getOutputWithValue(OUTPUT_VALUE_2)).contains(OUTPUT_2);
    }

    @Test
    void getOutputWithValue_not_found() {
        assertThat(TRANSACTION.getOutputWithValue(OUTPUT_VALUE_2.add(Coins.ofSatoshis(1)))).isEmpty();
    }

    @Test
    void getOutputWithValue_not_unique() {
        Transaction transaction = new Transaction(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                Coins.NONE,
                List.of(new Input(OUTPUT_VALUE_1.add(OUTPUT_VALUE_1), "a")),
                List.of(OUTPUT_1, OUTPUT_1)
        );
        assertThat(transaction.getOutputWithValue(OUTPUT_VALUE_1)).isEmpty();
    }

    @Test
    void testToString() {
        List<Input> inputs = TRANSACTION.getInputs();
        List<Output> outputs = TRANSACTION.getOutputs();
        String expectedString = "Transaction{hash='%s', blockHeight=%s, time=%s, fees=%s, inputs=%s, outputs=%s}"
                .formatted(TRANSACTION_HASH, BLOCK_HEIGHT, DATE_TIME, FEES, inputs, outputs);
        assertThat(TRANSACTION).hasToString(expectedString);
    }

    @Test
    void testToString_unknown() {
        assertThat(new Transaction("", BLOCK_HEIGHT)).hasToString("Transaction{UNKNOWN}");
    }

    @Test
    void testToString_empty_hash() {
        assertThat(Transaction.UNKNOWN).hasToString("Transaction{UNKNOWN}");
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Transaction.class).usingGetClass().suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void testEquals_both_unknown() {
        assertThat(new Transaction("", BLOCK_HEIGHT)).isEqualTo(Transaction.UNKNOWN);
    }

    @Test
    void testEquals_one_is_unknown() {
        assertThat(TRANSACTION).isNotEqualTo(Transaction.UNKNOWN);
    }

    @Test
    void getBlockHeight() {
        assertThat(TRANSACTION.getBlockHeight()).isEqualTo(BLOCK_HEIGHT);
    }

    @Test
    void getHash() {
        assertThat(TRANSACTION.getHash()).isEqualTo(TRANSACTION_HASH);
    }

    @Test
    void getOutputs() {
        assertThat(TRANSACTION.getOutputs()).contains(OUTPUT_1);
    }

    @Test
    void ignoresEmptyOutputs() {
        List<Output> outputs = List.of(
                Output.EMPTY,
                new Output(Coins.NONE, "xx"),
                OUTPUT_1,
                OUTPUT_2
        );
        Transaction transaction =
                new Transaction(TRANSACTION_HASH, BLOCK_HEIGHT, DATE_TIME, FEES, TRANSACTION.getInputs(), outputs);
        assertThat(transaction.getOutputs()).isEqualTo(TRANSACTION.getOutputs());
    }

    @Test
    void outputs_are_umodifiable() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() ->
            TRANSACTION.getOutputs().clear()
        );
    }

    @Test
    void outputs_is_copy_of_original_list() {
        List<Output> outputs = new ArrayList<>();
        outputs.add(OUTPUT_1);
        outputs.add(OUTPUT_2);
        Transaction transaction =
                new Transaction(TRANSACTION_HASH, BLOCK_HEIGHT, DATE_TIME, FEES, TRANSACTION.getInputs(), outputs);
        outputs.clear();
        assertThat(transaction.getOutputs()).isEqualTo(TRANSACTION.getOutputs());
    }

    @Test
    void getInputs() {
        assertThat(TRANSACTION.getInputs()).contains(INPUT_1);
    }

    @Test
    void ignoresEmptyInputs() {
        List<Input> inputs = List.of(
                Input.EMPTY,
                new Input(Coins.NONE, "xx"),
                INPUT_1,
                INPUT_2
        );
        Transaction transaction =
                new Transaction(TRANSACTION_HASH, BLOCK_HEIGHT, DATE_TIME, FEES, inputs, TRANSACTION.getOutputs());
        assertThat(transaction.getInputs()).isEqualTo(TRANSACTION.getInputs());
    }

    @Test
    void inputs_are_umodifiable() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() ->
                TRANSACTION.getInputs().clear()
        );

    }

    @Test
    void inputs_is_copy_of_original_list() {
        List<Input> inputs = new ArrayList<>();
        inputs.add(INPUT_1);
        inputs.add(INPUT_2);
        List<Output> outputs = TRANSACTION.getOutputs();
        Transaction transaction = new Transaction(TRANSACTION_HASH, BLOCK_HEIGHT, DATE_TIME, FEES, inputs, outputs);
        inputs.clear();
        assertThat(transaction.getInputs()).isEqualTo(TRANSACTION.getInputs());
    }

    @Test
    void wrong_sum_of_coins() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                new Transaction(TRANSACTION_HASH, BLOCK_HEIGHT, DATE_TIME, FEES, List.of(), List.of())
        );
    }
}
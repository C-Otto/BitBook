package de.cotto.bitbook.backend.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_2;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_1;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_2;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_VALUE_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_2;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_VALUE_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_VALUE_2;
import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.model.TransactionFixtures.FEES;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH;
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
                List.of(OUTPUT_1, OUTPUT_2, new Output(Coins.NONE, new Address("xxx")))
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
        Transaction transaction = getTransaction(coinsIn, Coins.ofSatoshis(5));
        Coins difference = Coins.NONE.subtract(coinsIn);
        assertThat(transaction.getDifferenceForAddress(ADDRESS)).isEqualTo(difference);
    }

    @Test
    void getDifferenceForAddresses() {
        Transaction transaction = getTransaction(Coins.ofSatoshis(10), Coins.ofSatoshis(5));
        Coins expectedDifference = Coins.NONE.subtract(transaction.getFees());
        assertThat(transaction.getDifferenceForAddresses(Set.of(ADDRESS, ADDRESS_2))).isEqualTo(expectedDifference);
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
                List.of(new Input(OUTPUT_VALUE_1.add(OUTPUT_VALUE_1), new Address("a"))),
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
    void getTime() {
        assertThat(TRANSACTION.getTime()).isEqualTo(DATE_TIME);
    }

    @Test
    void getTime_without_nanoseconds() {
        Transaction transaction = new Transaction(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME.withNano(789),
                FEES,
                List.of(INPUT_1, INPUT_2),
                List.of(OUTPUT_1, OUTPUT_2)
        );
        assertThat(transaction.getTime()).isEqualTo(DATE_TIME);
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
                new Output(Coins.NONE, new Address("xx")),
                OUTPUT_1,
                OUTPUT_2
        );
        Transaction transaction =
                new Transaction(TRANSACTION_HASH, BLOCK_HEIGHT, DATE_TIME, FEES, TRANSACTION.getInputs(), outputs);
        assertThat(transaction.getOutputs()).isEqualTo(TRANSACTION.getOutputs());
    }

    @Test
    void outputs_are_unmodifiable() {
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
                new Input(Coins.NONE, new Address("xx")),
                INPUT_1,
                INPUT_2
        );
        Transaction transaction =
                new Transaction(TRANSACTION_HASH, BLOCK_HEIGHT, DATE_TIME, FEES, inputs, TRANSACTION.getOutputs());
        assertThat(transaction.getInputs()).isEqualTo(TRANSACTION.getInputs());
    }

    @Test
    void inputs_are_unmodifiable() {
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

    private Transaction getTransaction(Coins inFromAddress, Coins outToAddress2) {
        return new Transaction(
                TRANSACTION_HASH,
                BLOCK_HEIGHT,
                DATE_TIME,
                inFromAddress.subtract(outToAddress2),
                List.of(new Input(inFromAddress, ADDRESS)),
                List.of(new Output(outToAddress2, ADDRESS_2))
        );
    }
}
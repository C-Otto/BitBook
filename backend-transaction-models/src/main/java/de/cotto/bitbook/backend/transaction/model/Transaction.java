package de.cotto.bitbook.backend.transaction.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class Transaction {
    private static final LocalDateTime UNKNOWN_DATE_TIME = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
    private static final String COINBASE_ADDRESS = "coinbase";
    public static final Transaction UNKNOWN = new Transaction("", 0);

    private final String hash;
    private final int blockHeight;
    private final LocalDateTime time;
    private final Coins fees;
    private final List<Input> inputs;
    private final List<Output> outputs;

    public Transaction(
            String hash,
            int blockHeight,
            LocalDateTime time,
            Coins fees,
            List<Input> inputs,
            List<Output> outputs
    ) {
        this.hash = hash;
        this.blockHeight = blockHeight;
        this.time = time.withNano(0);
        this.fees = fees;
        this.inputs = inputs.stream().filter(input -> input.getValue().getSatoshis() > 0).collect(toList());
        this.outputs = outputs.stream().filter(output -> output.getValue().getSatoshis() > 0).collect(toList());
        validateCoinsSum(hash, fees, inputs, outputs);
    }

    public Transaction(String hash, int blockHeight) {
        this(hash, blockHeight, UNKNOWN_DATE_TIME, Coins.NONE, List.of(), List.of());
    }

    public static Transaction forCoinbase(
            String hash,
            int blockHeight,
            LocalDateTime time,
            Coins fees,
            List<Output> outputs
    ) {
        Input coinbaseInput = new Input(
                outputs.stream().map(InputOutput::getValue).reduce(fees, Coins::add),
                COINBASE_ADDRESS
        );
        return new Transaction(hash, blockHeight, time, fees, List.of(coinbaseInput), outputs);
    }

    public List<InputOutput> getIncomingToAndOutgoingFromAddress(String address) {
        if (getIncomingCoins(address).getSatoshis() > 0) {
            return new ArrayList<>(inputs);
        }
        if (getOutgoingCoins(address).getSatoshis() > 0) {
            return new ArrayList<>(outputs);
        }
        return List.of();
    }

    public Coins getIncomingCoins(String address) {
        return outputs.stream().filter(input -> input.getAddress().equals(address))
                .map(InputOutput::getValue)
                .reduce(Coins.NONE, Coins::add);
    }

    public Coins getOutgoingCoins(String address) {
        return inputs.stream().filter(input -> input.getAddress().equals(address))
                .map(InputOutput::getValue)
                .reduce(Coins.NONE, Coins::add);
    }

    public Coins getDifferenceForAddress(String address) {
        return getIncomingCoins(address).subtract(getOutgoingCoins(address));
    }

    public Coins getDifferenceForAddresses(Set<String> addresses) {
        return addresses.stream().map(this::getDifferenceForAddress).reduce(Coins.NONE, Coins::add);
    }

    public String getHash() {
        return hash;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Coins getFees() {
        return fees;
    }

    public List<Input> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    public List<Output> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public boolean isValid() {
        return !isInvalid();
    }

    public boolean isInvalid() {
        return hash.isBlank();
    }

    public Set<String> getAllAddresses() {
        return Sets.union(getInputAddresses(), getOutputAddresses());
    }

    public Set<String> getInputAddresses() {
        return inputs.stream().map(InputOutput::getAddress).collect(toSet());
    }

    public Set<String> getOutputAddresses() {
        return outputs.stream().map(InputOutput::getAddress).collect(toSet());
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public Optional<Output> getOutputWithValue(Coins expectedValue) {
        List<Output> candidates = getOutputs().stream()
                .filter(output -> expectedValue.equals(output.getValue()))
                .collect(toList());
        if (candidates.size() == 1) {
            return Optional.of(candidates.get(0));
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        if (isValid()) {
            return "Transaction{" +
                    "hash='" + hash + '\'' +
                    ", blockHeight=" + blockHeight +
                    ", time=" + time +
                    ", fees=" + fees +
                    ", inputs=" + inputs +
                    ", outputs=" + outputs +
                    '}';
        }
        return "Transaction{UNKNOWN}";
    }

    @Override
    @SuppressWarnings({"PMD.NPathComplexity", "PMD.CyclomaticComplexity"})
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Transaction that = (Transaction) other;
        if (isValid() == that.isValid() && !isValid()) {
            return true;
        }

        if (blockHeight != that.blockHeight) {
            return false;
        }
        if (!Objects.equals(hash, that.hash)) {
            return false;
        }
        if (!Objects.equals(fees, that.fees)) {
            return false;
        }
        if (!Objects.equals(inputs, that.inputs)) {
            return false;
        }
        if (!Objects.equals(outputs, that.outputs)) {
            return false;
        }
        return Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        int result = hash.hashCode();
        result = 31 * result + blockHeight;
        result = 31 * result + time.hashCode();
        result = 31 * result + fees.hashCode();
        result = 31 * result + inputs.hashCode();
        result = 31 * result + outputs.hashCode();
        return result;
    }

    private void validateCoinsSum(String hash, Coins fees, List<Input> inputs, List<Output> outputs) {
        Coins sumInputs = inputs.stream().map(InputOutput::getValue).reduce(Coins.NONE, Coins::add);
        Coins sumOutputs = outputs.stream().map(InputOutput::getValue).reduce(Coins.NONE, Coins::add);
        Coins total = sumInputs.subtract(sumOutputs).subtract(fees);
        String errorMessage = hash + ": inputs %s - outputs %s - fee %s must be 0 but is %s".formatted(
                sumInputs, sumOutputs, fees, total
        );
        Preconditions.checkArgument(total.equals(Coins.NONE), errorMessage);
    }
}

package de.cotto.bitbook.backend.model;

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
    private static final Address COINBASE_ADDRESS = new Address("coinbase");

    private final TransactionHash hash;
    private final int blockHeight;
    private final LocalDateTime time;
    private final Coins fees;
    private final List<Input> inputs;
    private final List<Output> outputs;
    private final Chain chain;

    public Transaction(
            TransactionHash hash,
            int blockHeight,
            LocalDateTime time,
            Coins fees,
            List<Input> inputs,
            List<Output> outputs,
            Chain chain
    ) {
        this.hash = hash;
        this.blockHeight = blockHeight;
        this.time = time.withNano(0);
        this.fees = fees;
        this.inputs = inputs.stream().filter(input -> input.getValue().satoshis() > 0).collect(toList());
        this.outputs = new ArrayList<>(outputs);
        this.chain = chain;
        validateCoinsSum(hash, fees, inputs, outputs);
    }

    public Transaction(TransactionHash hash, int blockHeight, Chain chain) {
        this(hash, blockHeight, UNKNOWN_DATE_TIME, Coins.NONE, List.of(), List.of(), chain);
    }

    public static Transaction unknown(Chain chain) {
        return new Transaction(TransactionHash.NONE, 0, chain);
    }

    public static Transaction forCoinbase(
            TransactionHash hash,
            int blockHeight,
            LocalDateTime time,
            Coins fees,
            List<Output> outputs,
            Chain chain
    ) {
        Input coinbaseInput = new Input(
                outputs.stream().map(InputOutput::getValue).reduce(fees, Coins::add),
                COINBASE_ADDRESS
        );
        return new Transaction(hash, blockHeight, time, fees, List.of(coinbaseInput), outputs, chain);
    }

    public List<InputOutput> getIncomingToAndOutgoingFromAddress(Address address) {
        if (getIncomingCoins(address).satoshis() > 0) {
            return new ArrayList<>(inputs);
        }
        if (getOutgoingCoins(address).satoshis() > 0) {
            return new ArrayList<>(outputs);
        }
        return List.of();
    }

    public Coins getIncomingCoins(Address address) {
        return outputs.stream().filter(input -> input.getAddress().equals(address))
                .map(InputOutput::getValue)
                .reduce(Coins.NONE, Coins::add);
    }

    public Coins getOutgoingCoins(Address address) {
        return inputs.stream().filter(input -> input.getAddress().equals(address))
                .map(InputOutput::getValue)
                .reduce(Coins.NONE, Coins::add);
    }

    public Coins getDifferenceForAddress(Address address) {
        return getIncomingCoins(address).subtract(getOutgoingCoins(address));
    }

    public Coins getDifferenceForAddresses(Set<Address> addresses) {
        return addresses.stream().map(this::getDifferenceForAddress).reduce(Coins.NONE, Coins::add);
    }

    public TransactionHash getHash() {
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
        return hash.isInvalid();
    }

    public Set<Address> getAllAddresses() {
        return Sets.union(getInputAddresses(), getOutputAddresses());
    }

    public Set<Address> getInputAddresses() {
        return inputs.stream().map(InputOutput::getAddress).collect(toSet());
    }

    public Set<Address> getOutputAddresses() {
        return outputs.stream().map(InputOutput::getAddress).collect(toSet());
    }

    public Chain getChain() {
        return chain;
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public Optional<Output> getOutputWithValue(Coins expectedValue) {
        List<Output> candidates = getOutputs().stream()
                .filter(output -> expectedValue.equals(output.getValue()))
                .toList();
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
                    ", chain='" + chain + '\'' +
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
            return chain.equals(that.chain);
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
        if (!Objects.equals(chain, that.chain)) {
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
        result = 31 * result + chain.hashCode();
        return result;
    }

    private void validateCoinsSum(TransactionHash hash, Coins fees, List<Input> inputs, List<Output> outputs) {
        Coins sumInputs = inputs.stream().map(InputOutput::getValue).reduce(Coins.NONE, Coins::add);
        Coins sumOutputs = outputs.stream().map(InputOutput::getValue).reduce(Coins.NONE, Coins::add);
        Coins total = sumInputs.subtract(sumOutputs).subtract(fees);
        String errorMessage = hash + ": inputs %s - outputs %s - fee %s must be 0 but is %s".formatted(
                sumInputs, sumOutputs, fees, total
        );
        Preconditions.checkArgument(total.equals(Coins.NONE), errorMessage);
    }
}

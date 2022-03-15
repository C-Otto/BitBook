package de.cotto.bitbook.backend.model;

import com.google.common.base.Preconditions;

public class InputOutput {
    protected final Coins value;
    protected final Address address;

    protected InputOutput(Coins value, Address targetAddress) {
        Preconditions.checkArgument(value.satoshis() >= 0);
        this.value = value;
        this.address = targetAddress;
    }

    @Override
    public String toString() {
        String className = getClass().getSimpleName();
        return className + "{" +
               "value=" + value +
               ", address='" + address + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        InputOutput inputOutput = (InputOutput) other;

        if (!value.equals(inputOutput.value)) {
            return false;
        }
        return address.equals(inputOutput.address);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + address.hashCode();
        return result;
    }

    public Address getAddress() {
        return address;
    }

    public Coins getValue() {
        return value;
    }
}

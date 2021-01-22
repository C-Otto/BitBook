package de.cotto.bitbook.backend.model;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Objects;

public class AddressWithDescription implements Comparable<AddressWithDescription> {
    private final String address;
    private final String description;

    public AddressWithDescription(String address) {
        this(address, "");
    }

    public AddressWithDescription(String address, String description) {
        this.address = address;
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        AddressWithDescription that = (AddressWithDescription) other;
        return Objects.equals(address, that.address) && Objects.equals(description, that.description);
    }

    @Override
    public int compareTo(@Nonnull AddressWithDescription other) {
        return Comparator.comparing(AddressWithDescription::getDescription)
                .thenComparing(AddressWithDescription::getAddress)
                .compare(this, other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, description);
    }

    @Override
    public String toString() {
        return getFormattedAddress() + ' ' + getFormattedDescription();
    }

    public String getFormattedAddress() {
        return padOrShorten(address, 45);
    }

    public String getFormattedDescription() {
        return padOrShorten(description, 20);
    }

    private String padOrShorten(String string, int limit) {
        if (string.length() > limit) {
            return string.substring(0, limit - 1) + "…";
        } else {
            return StringUtils.leftPad(string, limit);
        }
    }

    public String getFormattedWithInfix(Object infixObject) {
        return getFormattedAddress() + ' ' + infixObject + ' ' + getFormattedDescription();
    }
}

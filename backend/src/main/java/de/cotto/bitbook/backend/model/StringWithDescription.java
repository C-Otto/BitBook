package de.cotto.bitbook.backend.model;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Objects;

public abstract class StringWithDescription<T extends StringWithDescription<T>> implements Comparable<T>  {
    private final String string;
    private final String description;

    public StringWithDescription(String string, String description) {
        this.string = string;
        this.description = description;
    }

    public String getString() {
        return string;
    }

    public String getDescription() {
        return description;
    }

    protected abstract String getFormattedString();

    public String getFormattedDescription() {
        return padOrShorten(description, 20);
    }

    @Override
    public String toString() {
        return getFormattedString() + ' ' + getFormattedDescription();
    }

    public String getFormattedWithInfix(Object infixObject) {
        return getFormattedString() + ' ' + infixObject + ' ' + getFormattedDescription();
    }

    @Override
    public int compareTo(@Nonnull T other) {
        return Comparator.<StringWithDescription<T>, String>comparing(StringWithDescription::getDescription)
                .thenComparing(StringWithDescription::getString)
                .compare(this, other);
    }

    protected String padOrShorten(String string, int limit) {
        if (string.length() > limit) {
            return string.substring(0, limit - 1) + "…";
        } else {
            return StringUtils.leftPad(string, limit);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        StringWithDescription<?> that = (StringWithDescription<?>) other;
        return Objects.equals(string, that.string) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(string, description);
    }
}

package de.cotto.bitbook.backend.model;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Objects;

public abstract class ModelWithDescription<M extends Comparable<M>, T extends ModelWithDescription<M, T>>
        implements Comparable<T>  {
    private final M model;
    private final String description;

    public ModelWithDescription(M model, String description) {
        this.model = model;
        this.description = description;
    }

    public M getModel() {
        return model;
    }

    public String getDescription() {
        return description;
    }

    protected abstract String getFormattedString();

    public String getFormattedDescription() {
        return shortenIfNecessary(description, 40);
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
        return Comparator.<ModelWithDescription<M, T>, String>comparing(ModelWithDescription::getDescription)
                .thenComparing(ModelWithDescription::getModel)
                .compare(this, other);
    }

    protected String padOrShorten(String string, @SuppressWarnings("SameParameterValue") int limit) {
        return StringUtils.leftPad(shortenIfNecessary(string, limit), limit);
    }

    protected String shortenIfNecessary(String string, int limit) {
        if (string.length() > limit) {
            return string.substring(0, limit - 1) + "…";
        }
        return string;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ModelWithDescription<?, ?> that = (ModelWithDescription<?, ?>) other;
        return Objects.equals(model, that.model) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, description);
    }
}

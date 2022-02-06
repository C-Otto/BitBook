package de.cotto.bitbook.backend.transaction.deserialization;

import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.InputOutput;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public abstract class InputOutputDto {
    @Nullable
    private Coins value;

    @Nullable
    private String address;

    public InputOutputDto() {
        // for Jackson
    }

    public InputOutputDto(@Nonnull Coins value, @Nonnull String address) {
        this.address = address;
        this.value = value;
    }

    public abstract InputOutput toModel();

    protected Coins getValue() {
        return Objects.requireNonNull(value);
    }

    public void setValue(@Nonnull Coins value) {
        this.value = value;
    }

    public String getAddress() {
        return Objects.requireNonNull(address);
    }

    public void setAddress(@Nonnull String address) {
        this.address = address;
    }
}

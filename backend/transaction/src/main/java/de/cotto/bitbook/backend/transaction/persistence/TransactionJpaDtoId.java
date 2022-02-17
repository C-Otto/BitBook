package de.cotto.bitbook.backend.transaction.persistence;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

public class TransactionJpaDtoId implements Serializable {
    @Nullable
    private String hash;

    @Nullable
    private String chain;

    @SuppressWarnings("unused")
    public TransactionJpaDtoId() {
        // for JPA
    }

    public TransactionJpaDtoId(@Nonnull String hash, @Nonnull String chain) {
        this.hash = hash;
        this.chain = chain;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        TransactionJpaDtoId that = (TransactionJpaDtoId) other;
        return Objects.equals(hash, that.hash) && Objects.equals(chain, that.chain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, chain);
    }
}

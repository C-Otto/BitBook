package de.cotto.bitbook.backend.transaction.persistence;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.Chain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

public class AddressTransactionsJpaDtoId implements Serializable {
    @Nullable
    private String address;

    @Nullable
    private String chain;

    @SuppressWarnings("unused")
    public AddressTransactionsJpaDtoId() {
        // for JPA
    }

    public AddressTransactionsJpaDtoId(@Nonnull String address, @Nonnull String chain) {
        this.address = address;
        this.chain = chain;
    }

    public static AddressTransactionsJpaDtoId fromModels(Address address, Chain chain) {
        return new AddressTransactionsJpaDtoId(address.toString(), chain.toString());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        AddressTransactionsJpaDtoId that = (AddressTransactionsJpaDtoId) other;
        return Objects.equals(address, that.address) && Objects.equals(chain, that.chain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, chain);
    }
}

package de.cotto.bitbook.ownership.persistence;

import de.cotto.bitbook.ownership.OwnershipStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "AddressOwnership")
public class AddressOwnershipJpaDto {
    @Id
    @Nullable
    private String address;

    @Nullable
    @Enumerated(EnumType.STRING)
    private OwnershipStatus ownershipStatus;

    public AddressOwnershipJpaDto() {
        // for JPA
    }

    public AddressOwnershipJpaDto(@Nonnull String address, @Nonnull OwnershipStatus ownershipStatus) {
        this.address = address;
        this.ownershipStatus = ownershipStatus;
    }

    public String getAddress() {
        return requireNonNull(address);
    }

    public OwnershipStatus getOwnershipStatus() {
        if (ownershipStatus == null) {
            return OwnershipStatus.UNKNOWN;
        }
        return ownershipStatus;
    }
}

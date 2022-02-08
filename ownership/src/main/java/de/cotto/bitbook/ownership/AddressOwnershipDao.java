package de.cotto.bitbook.ownership;

import de.cotto.bitbook.backend.model.Address;

import java.util.Set;

public interface AddressOwnershipDao {
    Set<Address> getOwnedAddresses();

    Set<Address> getForeignAddresses();

    void setAddressAsOwned(Address address);

    void setAddressAsForeign(Address address);

    void remove(Address address);

    OwnershipStatus getOwnershipStatus(Address address);
}

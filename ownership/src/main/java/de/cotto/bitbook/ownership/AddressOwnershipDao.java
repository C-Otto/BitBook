package de.cotto.bitbook.ownership;

import java.util.Set;

public interface AddressOwnershipDao {
    Set<String> getOwnedAddresses();

    Set<String> getForeignAddresses();

    void setAddressAsOwned(String address);

    void setAddressAsForeign(String address);

    void remove(String address);

    OwnershipStatus getOwnershipStatus(String address);
}

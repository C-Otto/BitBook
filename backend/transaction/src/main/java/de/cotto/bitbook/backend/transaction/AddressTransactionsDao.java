package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;

import java.util.Set;

public interface AddressTransactionsDao {
    void saveAddressTransactions(AddressTransactions addressTransactions);

    AddressTransactions getAddressTransactions(Address address);

    Set<Address> getAddressesStartingWith(String addressPrefix);
}

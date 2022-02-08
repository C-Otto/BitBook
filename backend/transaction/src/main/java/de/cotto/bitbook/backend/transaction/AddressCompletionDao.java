package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Address;

import java.util.Set;

public interface AddressCompletionDao {
    Set<Address> completeFromAddressTransactions(String prefix);

    Set<Address> completeFromInputsAndOutputs(String prefix);
}

package de.cotto.bitbook.backend.transaction;

import java.util.Set;

public interface AddressCompletionDao {
    Set<String> completeFromAddressTransactions(String prefix);

    Set<String> completeFromInputsAndOutputs(String prefix);
}

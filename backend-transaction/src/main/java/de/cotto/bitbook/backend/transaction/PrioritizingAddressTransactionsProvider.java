package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.Provider;
import de.cotto.bitbook.backend.request.PrioritizingProvider;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrioritizingAddressTransactionsProvider
        extends PrioritizingProvider<TransactionsRequestKey, AddressTransactions> {

    public PrioritizingAddressTransactionsProvider(
            List<Provider<TransactionsRequestKey, AddressTransactions>> providers
    ) {
        super(providers, "Transactions for address");
    }

    public AddressTransactions getAddressTransactions(AddressTransactionsRequest request) {
        return getForRequest(request).orElse(AddressTransactions.UNKNOWN);
    }
}

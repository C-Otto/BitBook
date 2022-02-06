package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.Provider;
import de.cotto.bitbook.backend.request.PrioritizingProvider;
import de.cotto.bitbook.backend.request.ResultFuture;
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

    public ResultFuture<AddressTransactions> getAddressTransactions(AddressTransactionsRequest request) {
        return getForRequest(request);
    }
}

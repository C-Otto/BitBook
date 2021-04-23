package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.Provider;
import de.cotto.bitbook.backend.request.PrioritizingProvider;
import de.cotto.bitbook.backend.request.ResultFuture;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrioritizingTransactionProvider extends PrioritizingProvider<String, Transaction> {
    public PrioritizingTransactionProvider(List<Provider<String, Transaction>> providers) {
        super(providers, "Transaction details");
    }

    public ResultFuture<Transaction> getTransaction(TransactionRequest transactionRequest) {
        return getForRequest(transactionRequest);
    }
}

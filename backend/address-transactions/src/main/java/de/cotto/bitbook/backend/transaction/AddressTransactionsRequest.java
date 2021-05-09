package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.request.PrioritizedRequest;
import de.cotto.bitbook.backend.request.RequestPriority;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;

public final class AddressTransactionsRequest extends PrioritizedRequest<TransactionsRequestKey, AddressTransactions> {
    private AddressTransactionsRequest(TransactionsRequestKey transactionsRequestKey, RequestPriority priority) {
        super(transactionsRequestKey, priority);
    }

    public static AddressTransactionsRequest create(
            TransactionsRequestKey transactionsRequestKey,
            RequestPriority requestPriority
    ) {
        return new AddressTransactionsRequest(transactionsRequestKey, requestPriority);
    }

    @Override
    public String toString() {
        return "AddressTransactionsRequest{" +
               "transactionsRequestKey='" + getKey() + '\'' +
               ", priority=" + getPriority() +
               '}';
    }
}

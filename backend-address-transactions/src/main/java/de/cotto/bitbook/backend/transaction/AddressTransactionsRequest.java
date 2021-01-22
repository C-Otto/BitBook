package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.request.PrioritizedRequest;
import de.cotto.bitbook.backend.request.RequestPriority;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;

import java.util.function.Consumer;

import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;

public final class AddressTransactionsRequest extends PrioritizedRequest<TransactionsRequestKey, AddressTransactions> {
    private AddressTransactionsRequest(TransactionsRequestKey transactionsRequestKey, RequestPriority priority) {
        this(transactionsRequestKey, priority, result -> {});
    }

    private AddressTransactionsRequest(
            TransactionsRequestKey transactionsRequestKey,
            RequestPriority priority,
            Consumer<AddressTransactions> resultConsumer
    ) {
        super(transactionsRequestKey, priority, resultConsumer);
    }

    public static AddressTransactionsRequest forLowestPriority(TransactionsRequestKey transactionsRequestKey) {
        return create(transactionsRequestKey, LOWEST);
    }

    public static AddressTransactionsRequest forStandardPriority(TransactionsRequestKey transactionsRequestKey) {
        return create(transactionsRequestKey, STANDARD);
    }

    public static AddressTransactionsRequest create(
            TransactionsRequestKey transactionsRequestKey,
            RequestPriority requestPriority
    ) {
        return new AddressTransactionsRequest(transactionsRequestKey, requestPriority);
    }

    public AddressTransactionsRequest getWithResultConsumer(Consumer<AddressTransactions> resultConsumer) {
        return new AddressTransactionsRequest(getKey(), getPriority(), resultConsumer);
    }

    @Override
    public String toString() {
        return "AddressTransactionsRequest{" +
               "transactionsRequestKey='" + getKey() + '\'' +
               ", priority=" + getPriority() +
               '}';
    }
}

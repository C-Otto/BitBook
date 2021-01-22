package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.request.PrioritizedRequest;
import de.cotto.bitbook.backend.request.RequestPriority;
import de.cotto.bitbook.backend.transaction.model.Transaction;

import java.util.function.Consumer;

public final class TransactionRequest extends PrioritizedRequest<String, Transaction> {
    public TransactionRequest(String transactionHash, RequestPriority priority) {
        this(transactionHash, priority, result -> {});
    }

    public TransactionRequest(String transactionHash, RequestPriority priority, Consumer<Transaction> resultConsumer) {
        super(transactionHash, priority, resultConsumer);
    }

    public String getHash() {
        return getKey();
    }

    @Override
    public String toString() {
        return "TransactionRequest{" +
               "transactionHash='" + getHash() + '\'' +
               ", priority=" + getPriority() +
               '}';
    }
}

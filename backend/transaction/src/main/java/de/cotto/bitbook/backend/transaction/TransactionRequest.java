package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.request.PrioritizedRequest;
import de.cotto.bitbook.backend.request.RequestPriority;

public final class TransactionRequest extends PrioritizedRequest<TransactionHash, Transaction> {
    public TransactionRequest(TransactionHash transactionHash, RequestPriority priority) {
        super(transactionHash, priority);
    }

    public TransactionHash getHash() {
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

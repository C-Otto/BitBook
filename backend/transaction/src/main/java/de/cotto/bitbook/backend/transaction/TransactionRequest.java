package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.HashAndChain;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.request.PrioritizedRequest;
import de.cotto.bitbook.backend.request.RequestPriority;

public final class TransactionRequest extends PrioritizedRequest<HashAndChain, Transaction> {
    public TransactionRequest(TransactionHash transactionHash, Chain chain, RequestPriority priority) {
        super(new HashAndChain(transactionHash, chain), priority);
    }

    public HashAndChain getHashAndChain() {
        return getKey();
    }
}

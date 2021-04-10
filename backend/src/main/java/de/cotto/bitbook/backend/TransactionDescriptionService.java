package de.cotto.bitbook.backend;

import de.cotto.bitbook.backend.model.TransactionWithDescription;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TransactionDescriptionService {
    private static final int MINIMUM_LENGTH_FOR_COMPLETION = 3;
    private final TransactionWithDescriptionDao dao;

    public TransactionDescriptionService(TransactionWithDescriptionDao dao) {
        this.dao = dao;
    }

    public TransactionWithDescription get(String transactionHash) {
        return dao.get(transactionHash);
    }

    public void set(String transactionHash, String description) {
        if (description.isBlank()) {
            return;
        }
        dao.save(new TransactionWithDescription(transactionHash, description));
    }

    public void remove(String transactionHash) {
        dao.remove(transactionHash);
    }

    public Set<TransactionWithDescription> getTransactionsWithDescriptionInfix(String infix) {
        if (infix.length() < MINIMUM_LENGTH_FOR_COMPLETION) {
            return Set.of();
        }
        return dao.findWithDescriptionInfix(infix);
    }
}

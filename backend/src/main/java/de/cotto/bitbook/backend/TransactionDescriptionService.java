package de.cotto.bitbook.backend;

import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.model.TransactionWithDescription;
import org.springframework.stereotype.Component;

@Component
public class TransactionDescriptionService extends DescriptionService<TransactionHash, TransactionWithDescription> {
    public TransactionDescriptionService(TransactionWithDescriptionDao dao) {
        super(dao);
    }

    public void set(TransactionHash transactionHash, String description) {
        set(new TransactionWithDescription(transactionHash, description));
    }
}

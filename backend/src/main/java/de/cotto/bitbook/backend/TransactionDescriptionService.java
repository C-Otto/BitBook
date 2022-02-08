package de.cotto.bitbook.backend;

import de.cotto.bitbook.backend.model.TransactionWithDescription;
import org.springframework.stereotype.Component;

@Component
public class TransactionDescriptionService extends DescriptionService<String, TransactionWithDescription> {
    public TransactionDescriptionService(TransactionWithDescriptionDao dao) {
        super(dao);
    }

    public void set(String transactionHash, String description) {
        set(new TransactionWithDescription(transactionHash, description));
    }
}

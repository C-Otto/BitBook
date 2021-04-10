package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.TransactionWithDescription;
import de.cotto.bitbook.backend.transaction.TransactionCompletionDao;
import org.springframework.stereotype.Component;

@Component
public class TransactionHashCompletionProvider extends AbstractTransactionCompletionProvider {

    public TransactionHashCompletionProvider(
            TransactionCompletionDao transactionCompletionDao,
            TransactionDescriptionService transactionDescriptionService
    ) {
        super(transactionCompletionDao, transactionDescriptionService);
    }

    @Override
    protected boolean shouldConsider(TransactionWithDescription transactionWithDescription) {
        return true;
    }

}

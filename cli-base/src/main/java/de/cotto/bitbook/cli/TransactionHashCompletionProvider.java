package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.model.TransactionWithDescription;
import de.cotto.bitbook.backend.transaction.TransactionCompletionDao;
import org.springframework.stereotype.Component;

@Component
public class TransactionHashCompletionProvider extends AbstractTransactionCompletionProvider {

    public TransactionHashCompletionProvider(TransactionCompletionDao transactionCompletionDao) {
        super(transactionCompletionDao);
    }

    @Override
    protected boolean shouldConsider(TransactionWithDescription transactionWithDescription) {
        return true;
    }

}

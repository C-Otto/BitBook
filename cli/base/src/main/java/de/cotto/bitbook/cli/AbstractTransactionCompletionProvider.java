package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.TransactionWithDescription;
import de.cotto.bitbook.backend.transaction.TransactionCompletionDao;

import java.util.Set;
import java.util.function.Function;

public abstract class AbstractTransactionCompletionProvider
        extends AbstractCompletionProvider<String, TransactionWithDescription> {

    private final TransactionCompletionDao transactionCompletionDao;

    public AbstractTransactionCompletionProvider(
            TransactionCompletionDao transactionCompletionDao,
            TransactionDescriptionService transactionDescriptionService
    ) {
        super(transactionDescriptionService);
        this.transactionCompletionDao = transactionCompletionDao;
    }

    @Override
    protected Set<Function<String, Set<String>>> getStringCompleters() {
        return Set.of(
                transactionCompletionDao::completeFromTransactionDetails,
                transactionCompletionDao::completeFromAddressTransactionHashes
        );
    }
}

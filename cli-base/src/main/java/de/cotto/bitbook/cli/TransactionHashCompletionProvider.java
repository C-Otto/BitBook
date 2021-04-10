package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.transaction.TransactionCompletionDao;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProviderSupport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionHashCompletionProvider extends ValueProviderSupport {
    private static final int MINIMUM_LENGTH_FOR_COMPLETION = 3;

    private final TransactionCompletionDao transactionCompletionDao;

    public TransactionHashCompletionProvider(TransactionCompletionDao transactionCompletionDao) {
        super();
        this.transactionCompletionDao = transactionCompletionDao;
    }

    @Override
    public List<CompletionProposal> complete(
            MethodParameter methodParameter,
            CompletionContext completionContext,
            String[] hints
    ) {
        String prefix = completionContext.currentWordUpToCursor();
        if (prefix.length() < MINIMUM_LENGTH_FOR_COMPLETION) {
            return List.of();
        }
        return transactionCompletionDao.getTransactionHashesStartingWith(prefix).stream()
                .map(CompletionProposal::new)
                .collect(Collectors.toList());
    }

}

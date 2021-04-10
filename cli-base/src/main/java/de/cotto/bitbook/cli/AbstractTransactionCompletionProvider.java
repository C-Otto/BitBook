package de.cotto.bitbook.cli;

import com.google.common.collect.Streams;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.TransactionWithDescription;
import de.cotto.bitbook.backend.transaction.TransactionCompletionDao;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProviderSupport;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractTransactionCompletionProvider extends ValueProviderSupport {
    private static final int MINIMUM_LENGTH_FOR_COMPLETION = 3;

    private final TransactionCompletionDao transactionCompletionDao;
    private final TransactionDescriptionService transactionDescriptionService;

    public AbstractTransactionCompletionProvider(
            TransactionCompletionDao transactionCompletionDao,
            TransactionDescriptionService transactionDescriptionService
    ) {
        super();
        this.transactionCompletionDao = transactionCompletionDao;
        this.transactionDescriptionService = transactionDescriptionService;
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
        Stream<String> fromTransactionDetails =
                transactionCompletionDao.completeFromTransactionDetails(prefix).stream();
        Stream<String> fromAddressTransactionHashes =
                transactionCompletionDao.completeFromAddressTransactionHashes(prefix).stream();
        return Streams.concat(fromTransactionDetails, fromAddressTransactionHashes)
                .distinct()
                .sorted()
                .map(transactionDescriptionService::get)
                .filter(this::shouldConsider)
                .map(this::getCompletionProposal)
                .collect(Collectors.toList());
    }

    private CompletionProposal getCompletionProposal(TransactionWithDescription transactionWithDescription) {
        String description = transactionWithDescription.getDescription();
        CompletionProposal completionProposal = new CompletionProposal(transactionWithDescription.getTransactionHash());
        if (description.isEmpty()) {
            return completionProposal;
        }
        return completionProposal.description(description);
    }

    protected abstract boolean shouldConsider(TransactionWithDescription transactionWithDescription);
}

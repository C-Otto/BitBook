package de.cotto.bitbook.cli;

import com.google.common.base.Functions;
import com.google.common.collect.Streams;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.TransactionWithDescription;
import de.cotto.bitbook.backend.transaction.TransactionCompletionDao;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
        String input = completionContext.currentWordUpToCursor();
        if (input.length() < MINIMUM_LENGTH_FOR_COMPLETION) {
            return List.of();
        }
        Stream<CompletionProposal> fromTransactionDetails = toProposals(
                transactionCompletionDao.completeFromTransactionDetails(input)
        );
        Stream<CompletionProposal> fromAddressTransactionHashes = toProposals(
                transactionCompletionDao.completeFromAddressTransactionHashes(input)
        );
        Stream<CompletionProposal> completedDescriptions =
                transactionDescriptionService.getTransactionsWithDescriptionInfix(input).stream()
                        .map(this::getCompletionProposalWithDescriptionInValue);
        return Streams.concat(fromTransactionDetails, fromAddressTransactionHashes, completedDescriptions)
                .collect(toMap(CompletionProposal::value, Functions.identity(), (a, b) -> a))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(toList());
    }

    private Stream<CompletionProposal> toProposals(Set<String> transactionHashes) {
        return transactionHashes.stream()
                .map(transactionDescriptionService::get)
                .filter(this::shouldConsider)
                .map(this::getCompletionProposal);
    }

    private CompletionProposal getCompletionProposal(TransactionWithDescription transactionWithDescription) {
        String description = transactionWithDescription.getDescription();
        CompletionProposal completionProposal = new CompletionProposal(transactionWithDescription.getTransactionHash());
        if (description.isEmpty()) {
            return completionProposal;
        }
        return completionProposal.description(description);
    }

    private CompletionProposal getCompletionProposalWithDescriptionInValue(
            TransactionWithDescription transactionWithDescription
    ) {
        // JLine does not show completion proposals that don't contain the text typed in by the user.
        // To work around this, we just include the description in the actual value (and remove it later).
        String address = transactionWithDescription.getTransactionHash();
        String separator = "\u00a0";
        String description = "(" + transactionWithDescription.getDescription() + ")";
        String ansiDescription = AnsiOutput.toString(AnsiColor.BRIGHT_BLACK, description, AnsiColor.DEFAULT);
        return new CompletionProposal(address + separator + ansiDescription);
    }

    protected abstract boolean shouldConsider(TransactionWithDescription transactionWithDescription);
}

package de.cotto.bitbook.cli;

import com.google.common.collect.Streams;
import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.transaction.AddressCompletionDao;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProviderSupport;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractAddressCompletionProvider extends ValueProviderSupport {
    private static final int MINIMUM_LENGTH_FOR_COMPLETION = 3;

    protected final AddressDescriptionService addressDescriptionService;
    private final AddressCompletionDao addressCompletionDao;

    public AbstractAddressCompletionProvider(
            AddressDescriptionService addressDescriptionService,
            AddressCompletionDao addressCompletionDao
    ) {
        super();
        this.addressDescriptionService = addressDescriptionService;
        this.addressCompletionDao = addressCompletionDao;
    }

    @Override
    public List<CompletionProposal> complete(
            MethodParameter parameter,
            CompletionContext completionContext,
            String[] hints
    ) {
        String input = completionContext.currentWordUpToCursor();
        if (isTooShort(input)) {
            return List.of();
        }
        Stream<CompletionProposal> completedFromAddressTransactions =
                addressCompletionDao.completeFromAddressTransactions(input).stream()
                .map(addressDescriptionService::get)
                .filter(this::shouldConsider)
                .map(this::getCompletionProposal);
        Stream<CompletionProposal> completedFromInputOutputs =
                addressCompletionDao.completeFromInputsAndOutputs(input).stream()
                        .map(addressDescriptionService::get)
                        .filter(this::shouldConsider)
                        .map(this::getCompletionProposal);
        Stream<CompletionProposal> completedDescriptions =
                addressDescriptionService.getAddressesWithDescriptionInfix(input).stream()
                        .map(this::getCompletionProposalWithDescriptionInValue);
        return Streams.concat(completedFromAddressTransactions, completedFromInputOutputs, completedDescriptions)
                .sorted(Comparator.comparing(CompletionProposal::value))
                .collect(Collectors.toList());
    }

    protected abstract boolean shouldConsider(AddressWithDescription addressWithDescription);

    private boolean isTooShort(String input) {
        if (input.startsWith("bc1")) {
            return input.length() < MINIMUM_LENGTH_FOR_COMPLETION + 3;
        }
        return input.length() < MINIMUM_LENGTH_FOR_COMPLETION;
    }

    private CompletionProposal getCompletionProposal(AddressWithDescription addressWithDescription) {
        String description = addressWithDescription.getDescription();
        CompletionProposal completionProposal = new CompletionProposal(addressWithDescription.getAddress());
        if (description.isEmpty()) {
            return completionProposal;
        }
        return completionProposal.description(description);
    }

    private CompletionProposal getCompletionProposalWithDescriptionInValue(
            AddressWithDescription addressWithDescription
    ) {
        // JLine does not show completion proposals that don't contain the text typed in by the user.
        // To work around this, we just include the description in the actual value (and remove it later).
        String address = addressWithDescription.getAddress();
        String separator = "\u00a0";
        String description = "(" + addressWithDescription.getDescription() + ")";
        String ansiDescription = AnsiOutput.toString(AnsiColor.BRIGHT_BLACK, description, AnsiColor.DEFAULT);
        return new CompletionProposal(address + separator + ansiDescription);
    }
}

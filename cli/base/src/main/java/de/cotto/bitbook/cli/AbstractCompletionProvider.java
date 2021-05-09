package de.cotto.bitbook.cli;

import com.google.common.base.Functions;
import com.google.common.collect.Streams;
import de.cotto.bitbook.backend.DescriptionService;
import de.cotto.bitbook.backend.model.StringWithDescription;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProviderSupport;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public abstract class AbstractCompletionProvider<T extends StringWithDescription<T>> extends ValueProviderSupport {
    private static final int MINIMUM_LENGTH_FOR_COMPLETION = 3;
    protected final DescriptionService<T> descriptionService;

    public AbstractCompletionProvider(DescriptionService<T> descriptionService) {
        super();
        this.descriptionService = descriptionService;
    }

    @Override
    public List<CompletionProposal> complete(
            MethodParameter methodParameter,
            CompletionContext completionContext,
            String[] hints
    ) {
        String input = getInputToComplete(completionContext);
        if (isTooShort(input, MINIMUM_LENGTH_FOR_COMPLETION)) {
            return List.of();
        }
        return getCompletionProposals(input)
                .collect(toMap(CompletionProposal::value, Functions.identity(), (a, b) -> a))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(toList());
    }

    @SuppressWarnings("SameParameterValue")
    protected boolean isTooShort(String input, int minimumLengthForCompletion) {
        return input.length() < minimumLengthForCompletion;
    }

    protected abstract Set<Function<String, Set<String>>> getStringCompleters();

    private String getInputToComplete(CompletionContext completionContext) {
        return completionContext.currentWordUpToCursor().trim().replaceAll("…", "");
    }

    private Stream<CompletionProposal> getCompletionProposals(String input) {
        Stream<CompletionProposal> completedInput = getStringCompleters().stream()
                .map(completer -> completer.apply(input))
                .flatMap(this::toProposals);
        Stream<CompletionProposal> completedDescription = completeDescription(input);
        return Streams.concat(completedInput, completedDescription);
    }

    protected Stream<CompletionProposal> toProposals(Set<String> strings) {
        return strings.stream()
                .map(descriptionService::get)
                .filter(this::shouldConsider)
                .map(this::getCompletionProposal);
    }

    protected abstract boolean shouldConsider(T stringWithDescription);

    private CompletionProposal getCompletionProposal(T stringWithDescription) {
        String description = stringWithDescription.getDescription();
        CompletionProposal completionProposal = new CompletionProposal(stringWithDescription.getString());
        if (description.isEmpty()) {
            return completionProposal;
        }
        return completionProposal.description(description);
    }

    private Stream<CompletionProposal> completeDescription(String input) {
        return descriptionService.getWithDescriptionInfix(input).stream()
                .map(this::getCompletionProposalWithDescriptionInValue);
    }

    private CompletionProposal getCompletionProposalWithDescriptionInValue(T stringWithDescription) {
        // JLine does not show completion proposals that don't contain the text typed in by the user.
        // To work around this, we just include the description in the actual value (and remove it later).
        String string = stringWithDescription.getString();
        String separator = "\u00a0";
        String description = "(" + stringWithDescription.getDescription() + ")";
        String ansiDescription = AnsiOutput.toString(AnsiColor.BRIGHT_BLACK, description, AnsiColor.DEFAULT);
        return new CompletionProposal(string + separator + ansiDescription);
    }
}

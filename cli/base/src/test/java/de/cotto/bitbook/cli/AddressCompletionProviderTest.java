package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.transaction.AddressCompletionDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;

import java.util.List;
import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_2;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressCompletionProviderTest {
    private static final Address BC1_ADDRESS = new Address("bc1xxx");
    private static final String PREFIX = "bc1xx";
    @InjectMocks
    private AddressCompletionProvider completionProvider;

    @Mock
    private AddressCompletionDao addressCompletionDao;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private CompletionContext context;

    private final String[] hints = new String[0];
    private String input;
    private String description;
    private AddressWithDescription addressWithDescription;

    @BeforeEach
    void setUp() {
        input = "abc";
        description = "my foo bar";
        addressWithDescription = new AddressWithDescription(ADDRESS, description);
    }

    @Test
    void complete_address() {
        when(context.currentWordUpToCursor()).thenReturn(input);
        when(addressDescriptionService.get(ADDRESS)).thenReturn(addressWithDescription);
        when(addressDescriptionService.get(ADDRESS_2)).thenReturn(new AddressWithDescription(ADDRESS_2));
        when(addressCompletionDao.completeFromAddressTransactions(input)).thenReturn(Set.of(ADDRESS, ADDRESS_2));

        List<CompletionProposal> complete = completionProvider.complete(methodParameter, context, hints);

        assertThat(complete).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new CompletionProposal(ADDRESS_2.toString()),
                new CompletionProposal(ADDRESS.toString()).description(description)
        );
    }

    @Test
    void complete_bc1_address() {
        input = BC1_ADDRESS.toString();
        assertCompletion(input);
    }

    @Test
    void complete_bc1_address_with_whitespace() {
        input = "      " + BC1_ADDRESS + "        ";
        assertCompletion(BC1_ADDRESS.toString());
    }

    @Test
    void complete_bc1_address_with_ellipsis() {
        input = BC1_ADDRESS + "…";
        assertCompletion(BC1_ADDRESS.toString());
    }

    private void assertCompletion(String expectedInput) {
        when(context.currentWordUpToCursor()).thenReturn(input);
        when(addressDescriptionService.get(ADDRESS)).thenReturn(addressWithDescription);
        when(addressCompletionDao.completeFromAddressTransactions(expectedInput)).thenReturn(Set.of(ADDRESS));

        assertThat(completionProvider.complete(methodParameter, context, hints)).isNotEmpty();
    }

    @Test
    void complete_address_from_input_output() {
        AddressWithDescription addressWithDescription = new AddressWithDescription(INPUT_ADDRESS_1, description);
        when(context.currentWordUpToCursor()).thenReturn(input);
        when(addressDescriptionService.get(INPUT_ADDRESS_1)).thenReturn(addressWithDescription);
        when(addressCompletionDao.completeFromInputsAndOutputs(input)).thenReturn(Set.of(INPUT_ADDRESS_1));

        List<CompletionProposal> complete = completionProvider.complete(methodParameter, context, hints);

        assertThat(complete).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new CompletionProposal(INPUT_ADDRESS_1.toString()).description(description)
        );
    }

    @Test
    void complete_no_duplicates() {
        AddressWithDescription addressWithDescription = new AddressWithDescription(ADDRESS);
        when(context.currentWordUpToCursor()).thenReturn(input);
        when(addressDescriptionService.get(ADDRESS)).thenReturn(addressWithDescription);
        when(addressCompletionDao.completeFromAddressTransactions(input)).thenReturn(Set.of(ADDRESS));
        when(addressCompletionDao.completeFromInputsAndOutputs(input)).thenReturn(Set.of(ADDRESS));

        List<CompletionProposal> complete = completionProvider.complete(methodParameter, context, hints);

        assertThat(complete).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new CompletionProposal(ADDRESS.toString())
        );
    }

    @Test
    void complete_description() {
        when(context.currentWordUpToCursor()).thenReturn(input);
        when(addressDescriptionService.getWithDescriptionInfix(input))
                .thenReturn(Set.of(addressWithDescription));

        List<CompletionProposal> complete = completionProvider.complete(methodParameter, context, hints);

        assertThat(complete).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new CompletionProposal(addressWithAnsiDescription())
        );
    }

    @Test
    void complete_sorted_by_address() {
        AddressWithDescription addressWithDescription = new AddressWithDescription(ADDRESS, description);
        when(context.currentWordUpToCursor()).thenReturn(input);
        when(addressDescriptionService.getWithDescriptionInfix(input))
                .thenReturn(Set.of(addressWithDescription));

        when(addressCompletionDao.completeFromAddressTransactions(input)).thenReturn(Set.of(ADDRESS_2));
        when(addressDescriptionService.get(ADDRESS_2)).thenReturn(new AddressWithDescription(ADDRESS_2));

        List<CompletionProposal> complete = completionProvider.complete(methodParameter, context, hints);

        assertThat(complete).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new CompletionProposal(ADDRESS_2.toString()),
                new CompletionProposal(addressWithAnsiDescription())
        );
    }

    @Test
    void does_not_complete_short_address() {
        when(context.currentWordUpToCursor()).thenReturn("xx");
        assertThat(completionProvider.complete(methodParameter, context, hints)).isEmpty();
        verifyNoInteractions(addressCompletionDao);
    }

    @Test
    void ignores_whitespace_when_checking_length() {
        when(context.currentWordUpToCursor()).thenReturn("xx             ");
        assertThat(completionProvider.complete(methodParameter, context, hints)).isEmpty();
        verifyNoInteractions(addressCompletionDao);
    }

    @Test
    void does_not_complete_short_bech32_address() {
        when(context.currentWordUpToCursor()).thenReturn("bc1xx");
        assertThat(completionProvider.complete(methodParameter, context, hints)).isEmpty();
        verifyNoInteractions(addressCompletionDao);
    }

    @Test
    void complete_single_string_not_unique() {
        when(addressCompletionDao.completeFromAddressTransactions(PREFIX)).thenReturn(Set.of(BC1_ADDRESS));
        when(addressCompletionDao.completeFromInputsAndOutputs(PREFIX)).thenReturn(Set.of(new Address("bc1xxy")));
        assertThat(completionProvider.completeIfUnique(PREFIX)).isEmpty();
    }

    @Test
    void complete_single_string_not_found() {
        assertThat(completionProvider.completeIfUnique(PREFIX)).isEmpty();
    }

    @Test
    void complete_single_string_unique() {
        when(addressCompletionDao.completeFromInputsAndOutputs(PREFIX)).thenReturn(Set.of(BC1_ADDRESS));
        assertThat(completionProvider.completeIfUnique(PREFIX)).contains(BC1_ADDRESS);
    }

    @Test
    void complete_after_trim_and_removing_ellipsis() {
        when(addressCompletionDao.completeFromInputsAndOutputs(PREFIX)).thenReturn(Set.of(BC1_ADDRESS));
        assertThat(completionProvider.completeIfUnique("   " + PREFIX + "…")).contains(BC1_ADDRESS);
    }

    private String addressWithAnsiDescription() {
        return ADDRESS + "\u00a0(" + AnsiOutput.toString(AnsiColor.BRIGHT_BLACK, description, AnsiColor.DEFAULT) + ")";
    }
}
package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.transaction.AddressCompletionDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;

import java.util.List;
import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressWithDescriptionCompletionProviderTest {
    private final String[] hints = new String[0];

    @InjectMocks
    private AddressWithDescriptionCompletionProvider completionProvider;

    @Mock
    private AddressCompletionDao addressCompletionDao;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private CompletionContext context;

    @Test
    void complete_address_only_with_description() {
        String input = "abc";
        String description1 = "my foo bar";
        Address address3 = new Address("foobar");
        String description3 = "blub";
        AddressWithDescription addressWithDescription1 = new AddressWithDescription(ADDRESS, description1);
        AddressWithDescription addressWithDescription2 = new AddressWithDescription(ADDRESS_2);
        AddressWithDescription addressWithDescription3 = new AddressWithDescription(address3, description3);
        when(context.currentWordUpToCursor()).thenReturn(input);
        when(addressDescriptionService.get(ADDRESS)).thenReturn(addressWithDescription1);
        when(addressDescriptionService.get(ADDRESS_2)).thenReturn(addressWithDescription2);
        when(addressDescriptionService.get(address3)).thenReturn(addressWithDescription3);
        when(addressCompletionDao.completeFromAddressTransactions(input))
                .thenReturn(Set.of(ADDRESS, ADDRESS_2, address3));

        List<CompletionProposal> complete = completionProvider.complete(methodParameter, context, hints);

        assertThat(complete).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new CompletionProposal(ADDRESS.toString()).description(description1),
                new CompletionProposal(address3.toString()).description(description3)
        );
    }
}
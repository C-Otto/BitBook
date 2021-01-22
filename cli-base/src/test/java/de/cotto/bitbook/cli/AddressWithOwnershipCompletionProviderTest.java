package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.transaction.AddressCompletionDao;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import de.cotto.bitbook.ownership.OwnershipStatus;
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

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_2;
import static de.cotto.bitbook.ownership.OwnershipStatus.FOREIGN;
import static de.cotto.bitbook.ownership.OwnershipStatus.OWNED;
import static de.cotto.bitbook.ownership.OwnershipStatus.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressWithOwnershipCompletionProviderTest {
    private final String[] hints = new String[0];
    @InjectMocks
    private AddressWithOwnershipCompletionProvider completionProvider;

    @Mock
    private AddressCompletionDao addressCompletionDao;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Mock
    private AddressOwnershipService addressOwnershipService;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private CompletionContext context;

    @Test
    void complete_address_only_with_known_ownership() {
        String input = "abc";
        String address3 = "foobar";
        when(addressDescriptionService.get(any()))
                .then(invocation -> new AddressWithDescription(invocation.getArgument(0)));
        when(context.currentWordUpToCursor()).thenReturn(input);
        when(addressCompletionDao.completeFromAddressTransactions(input))
                .thenReturn(Set.of(ADDRESS, ADDRESS_2, address3));

        mockOwnership(ADDRESS, UNKNOWN);
        mockOwnership(ADDRESS_2, OWNED);
        mockOwnership(address3, FOREIGN);
        List<CompletionProposal> complete = completionProvider.complete(methodParameter, context, hints);

        assertThat(complete).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new CompletionProposal(ADDRESS_2),
                new CompletionProposal(address3)
        );
    }

    private void mockOwnership(String address, OwnershipStatus ownershipStatus) {
        when(addressOwnershipService.getOwnershipStatus(address)).thenReturn(ownershipStatus);
    }
}
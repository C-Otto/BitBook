package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.Address;
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

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS_2;
import static de.cotto.bitbook.ownership.OwnershipStatus.FOREIGN;
import static de.cotto.bitbook.ownership.OwnershipStatus.OWNED;
import static de.cotto.bitbook.ownership.OwnershipStatus.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressWithOwnershipCompletionProviderTest {
    private static final String[] EMPTY_HINTS = new String[0];

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
        Address address3 = new Address("foobar");
        when(addressDescriptionService.get(any()))
                .then(invocation -> new AddressWithDescription(invocation.getArgument(0)));
        when(context.currentWordUpToCursor()).thenReturn(input);
        when(addressCompletionDao.completeFromAddressTransactions(input))
                .thenReturn(Set.of(ADDRESS, ADDRESS_2, address3));

        mockOwnership(ADDRESS, UNKNOWN);
        mockOwnership(ADDRESS_2, OWNED);
        mockOwnership(address3, FOREIGN);
        List<CompletionProposal> complete = completionProvider.complete(methodParameter, context, EMPTY_HINTS);

        assertThat(complete).usingRecursiveFieldByFieldElementComparator().containsExactly(
                new CompletionProposal(ADDRESS_2.toString()),
                new CompletionProposal(address3.toString())
        );
    }

    private void mockOwnership(Address address, OwnershipStatus ownershipStatus) {
        when(addressOwnershipService.getOwnershipStatus(address)).thenReturn(ownershipStatus);
    }
}

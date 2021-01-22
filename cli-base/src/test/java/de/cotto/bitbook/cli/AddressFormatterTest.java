package de.cotto.bitbook.cli;

import de.cotto.bitbook.ownership.AddressOwnershipService;
import de.cotto.bitbook.ownership.OwnershipStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressFormatterTest {
    @InjectMocks
    private AddressFormatter addressFormatter;

    @Mock
    private AddressOwnershipService addressOwnershipService;

    @Test
    void getFormattedOwnershipStatus_ownership_unknown() {
        when(addressOwnershipService.getOwnershipStatus(ADDRESS)).thenReturn(OwnershipStatus.UNKNOWN);
        assertThat(addressFormatter.getFormattedOwnershipStatus(ADDRESS)).isEqualTo("?");
    }

    @Test
    void getFormattedOwnershipStatus_ownership_owned() {
        when(addressOwnershipService.getOwnershipStatus(ADDRESS)).thenReturn(OwnershipStatus.OWNED);
        assertThat(addressFormatter.getFormattedOwnershipStatus(ADDRESS)).isEqualTo("✓");
    }

    @Test
    void getFormattedOwnershipStatus_ownership_foreign() {
        when(addressOwnershipService.getOwnershipStatus(ADDRESS)).thenReturn(OwnershipStatus.FOREIGN);
        assertThat(addressFormatter.getFormattedOwnershipStatus(ADDRESS)).isEqualTo("✗");
    }
}
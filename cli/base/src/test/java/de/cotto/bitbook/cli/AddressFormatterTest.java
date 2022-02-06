package de.cotto.bitbook.cli;

import de.cotto.bitbook.ownership.AddressOwnershipService;
import de.cotto.bitbook.ownership.OwnershipStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressFormatterTest {
    private static final String CHARACTER_FOREIGN = "✗";
    private static final String CHARACTER_OWNED = "✓";
    private static final String CHARACTER_UNKNOWN = "?";

    @InjectMocks
    private AddressFormatter addressFormatter;

    @Mock
    private AddressOwnershipService addressOwnershipService;

    @AfterEach
    void tearDown() {
        AnsiOutput.setEnabled(AnsiOutput.Enabled.DETECT);
    }

    @Test
    void getFormattedOwnershipStatus_ownership_unknown() {
        when(addressOwnershipService.getOwnershipStatus(ADDRESS)).thenReturn(OwnershipStatus.UNKNOWN);
        assertThat(addressFormatter.getFormattedOwnershipStatus(ADDRESS)).isEqualTo(CHARACTER_UNKNOWN);
    }

    @Test
    void getFormattedOwnershipStatus_ownership_owned() {
        when(addressOwnershipService.getOwnershipStatus(ADDRESS)).thenReturn(OwnershipStatus.OWNED);
        assertThat(addressFormatter.getFormattedOwnershipStatus(ADDRESS)).isEqualTo(CHARACTER_OWNED);
    }

    @Test
    void getFormattedOwnershipStatus_ownership_foreign() {
        when(addressOwnershipService.getOwnershipStatus(ADDRESS)).thenReturn(OwnershipStatus.FOREIGN);
        assertThat(addressFormatter.getFormattedOwnershipStatus(ADDRESS)).isEqualTo(CHARACTER_FOREIGN);
    }

    @Test
    void getFormattedOwnershipStatus_with_color_ownership_unknown() {
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
        when(addressOwnershipService.getOwnershipStatus(ADDRESS)).thenReturn(OwnershipStatus.UNKNOWN);
        assertColor(AnsiColor.BRIGHT_BLACK, CHARACTER_UNKNOWN);
    }

    @Test
    void getFormattedOwnershipStatus_with_color_ownership_owned() {
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
        when(addressOwnershipService.getOwnershipStatus(ADDRESS)).thenReturn(OwnershipStatus.OWNED);
        assertColor(AnsiColor.BRIGHT_GREEN, CHARACTER_OWNED);
    }

    @Test
    void getFormattedOwnershipStatus_with_color_ownership_foreign() {
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
        when(addressOwnershipService.getOwnershipStatus(ADDRESS)).thenReturn(OwnershipStatus.FOREIGN);
        assertColor(AnsiColor.BRIGHT_RED, CHARACTER_FOREIGN);
    }

    private void assertColor(AnsiColor expectedColor, String character) {
        assertThat(addressFormatter.getFormattedOwnershipStatus(ADDRESS))
                .isEqualTo(AnsiOutput.toString(expectedColor, character, AnsiColor.DEFAULT));
    }
}
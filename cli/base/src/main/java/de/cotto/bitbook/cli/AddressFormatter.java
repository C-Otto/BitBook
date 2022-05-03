package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import de.cotto.bitbook.ownership.OwnershipStatus;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.stereotype.Component;

@Component
public class AddressFormatter {
    private final AddressOwnershipService addressOwnershipService;

    public AddressFormatter(AddressOwnershipService addressOwnershipService) {
        this.addressOwnershipService = addressOwnershipService;
    }

    public String getFormattedOwnershipStatus(Address address) {
        OwnershipStatus ownershipStatus = addressOwnershipService.getOwnershipStatus(address);
        return AnsiOutput.toString(getColor(ownershipStatus), getCharacter(ownershipStatus), AnsiColor.DEFAULT);
    }

    private AnsiColor getColor(OwnershipStatus ownershipStatus) {
        return switch (ownershipStatus) {
            case UNKNOWN -> AnsiColor.BRIGHT_BLACK;
            case OWNED -> AnsiColor.BRIGHT_GREEN;
            case FOREIGN -> AnsiColor.BRIGHT_RED;
        };
    }

    private String getCharacter(OwnershipStatus ownershipStatus) {
        return switch (ownershipStatus) {
            case UNKNOWN -> "?";
            case OWNED -> "✓";
            case FOREIGN -> "✗";
        };
    }
}

package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.transaction.AddressCompletionDao;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import de.cotto.bitbook.ownership.OwnershipStatus;
import org.springframework.stereotype.Component;

@Component
public class AddressWithOwnershipCompletionProvider extends AbstractAddressCompletionProvider {

    private final AddressOwnershipService addressOwnershipService;

    public AddressWithOwnershipCompletionProvider(
            AddressDescriptionService addressDescriptionService,
            AddressOwnershipService addressOwnershipService,
            AddressCompletionDao addressCompletionDao
    ) {
        super(addressDescriptionService, addressCompletionDao);
        this.addressOwnershipService = addressOwnershipService;
    }

    @Override
    protected boolean shouldConsider(AddressWithDescription addressWithDescription) {
        String address = addressWithDescription.getAddress();
        return addressOwnershipService.getOwnershipStatus(address) != OwnershipStatus.UNKNOWN;
    }
}

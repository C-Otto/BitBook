package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.ownership.AddressOwnershipService;

public class AbstractTransactionsService {
    protected static final String DEFAULT_DESCRIPTION = "lnd";
    protected final AddressOwnershipService addressOwnershipService;
    protected final AddressDescriptionService addressDescriptionService;

    public AbstractTransactionsService(
            AddressOwnershipService addressOwnershipService,
            AddressDescriptionService addressDescriptionService
    ) {
        this.addressOwnershipService = addressOwnershipService;
        this.addressDescriptionService = addressDescriptionService;
    }

    protected void setAddressAsOwnedWithDescription(String address) {
        addressOwnershipService.setAddressAsOwned(address);
        addressDescriptionService.set(address, DEFAULT_DESCRIPTION);
    }
}

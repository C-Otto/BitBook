package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UnspentOutputsService {
    private static final String DEFAULT_ADDRESS_DESCRIPTION = "lnd";

    private final AddressOwnershipService addressOwnershipService;
    private final AddressDescriptionService addressDescriptionService;

    public UnspentOutputsService(
            AddressOwnershipService addressOwnershipService,
            AddressDescriptionService addressDescriptionService
    ) {
        this.addressOwnershipService = addressOwnershipService;
        this.addressDescriptionService = addressDescriptionService;
    }

    public long addFromUnspentOutputs(Set<Address> addresses) {
        addresses.forEach(addressOwnershipService::setAddressAsOwned);
        addresses.forEach(address -> addressDescriptionService.set(address, DEFAULT_ADDRESS_DESCRIPTION));
        return addresses.size();
    }
}

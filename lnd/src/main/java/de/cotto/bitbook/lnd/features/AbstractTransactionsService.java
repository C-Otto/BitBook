package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.ownership.AddressOwnershipService;

import static de.cotto.bitbook.backend.model.Chain.BTC;

public class AbstractTransactionsService {
    protected static final String DEFAULT_DESCRIPTION = "lnd";
    protected final AddressOwnershipService addressOwnershipService;
    protected final AddressDescriptionService addressDescriptionService;
    protected final TransactionDescriptionService transactionDescriptionService;
    protected final TransactionService transactionService;

    public AbstractTransactionsService(
            AddressOwnershipService addressOwnershipService,
            AddressDescriptionService addressDescriptionService,
            TransactionDescriptionService transactionDescriptionService,
            TransactionService transactionService
    ) {
        this.addressOwnershipService = addressOwnershipService;
        this.addressDescriptionService = addressDescriptionService;
        this.transactionDescriptionService = transactionDescriptionService;
        this.transactionService = transactionService;
    }

    protected void setAddressAsOwnedWithDescription(Address address) {
        addressOwnershipService.setAddressAsOwned(address, BTC);
        addressDescriptionService.set(address, DEFAULT_DESCRIPTION);
    }
}

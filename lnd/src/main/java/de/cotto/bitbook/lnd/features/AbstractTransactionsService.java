package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.ownership.AddressOwnershipService;

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

    protected void setAddressAsOwnedWithDescription(String address) {
        addressOwnershipService.setAddressAsOwned(address);
        addressDescriptionService.set(address, DEFAULT_DESCRIPTION);
    }
}

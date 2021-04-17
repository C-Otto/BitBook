package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Output;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import de.cotto.bitbook.ownership.AddressOwnershipService;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

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

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    protected <T> Optional<T> getIfExactlyOne(List<T> list) {
        if (list.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }

    protected List<String> getAddressForMatchingOutput(Transaction transactionDetails, Coins expectedValue) {
        return transactionDetails.getOutputs().stream()
                .filter(output -> expectedValue.equals(output.getValue()))
                .map(Output::getAddress)
                .collect(toList());
    }

    protected void setAddressAsOwnedWithDescription(String address) {
        addressOwnershipService.setAddressAsOwned(address);
        addressDescriptionService.set(address, DEFAULT_DESCRIPTION);
    }
}

package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.Input;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
public class SweepTransactionsService {
    private static final String SWEEP_TRANSACTION_DESCRIPTION = "lnd sweep transaction";
    private static final String DEFAULT_ADDRESS_DESCRIPTION = "lnd";

    private final TransactionService transactionService;
    private final AddressDescriptionService addressDescriptionService;
    private final AddressOwnershipService addressOwnershipService;
    private final TransactionDescriptionService transactionDescriptionService;

    public SweepTransactionsService(
            TransactionService transactionService,
            AddressDescriptionService addressDescriptionService,
            AddressOwnershipService addressOwnershipService,
            TransactionDescriptionService transactionDescriptionService
    ) {
        this.transactionService = transactionService;
        this.addressDescriptionService = addressDescriptionService;
        this.addressOwnershipService = addressOwnershipService;
        this.transactionDescriptionService = transactionDescriptionService;
    }

    public long addFromSweeps(Set<String> hashes) {
        Set<Transaction> sweepTransactions = transactionService.getTransactionDetails(hashes).stream()
                .filter(this::isSweepTransaction)
                .collect(toSet());
        sweepTransactions.forEach(transaction -> {
            addTransactionDescription(transaction);
            addAddressDescriptions(transaction);
            setAddressesAsOwned(transaction);
        });
        return sweepTransactions.size();
    }

    private boolean isSweepTransaction(Transaction transaction) {
        return transaction.getOutputs().size() == 1;
    }

    private void addAddressDescriptions(Transaction transaction) {
        getInputAddresses(transaction)
                .forEach(address -> addressDescriptionService.set(address, DEFAULT_ADDRESS_DESCRIPTION));
        addressDescriptionService.set(getOutputAddress(transaction), DEFAULT_ADDRESS_DESCRIPTION);
    }

    private void setAddressesAsOwned(Transaction transaction) {
        getInputAddresses(transaction).forEach(addressOwnershipService::setAddressAsOwned);
        addressOwnershipService.setAddressAsOwned(getOutputAddress(transaction));
    }

    private void addTransactionDescription(Transaction transaction) {
        transactionDescriptionService.set(transaction.getHash(), SWEEP_TRANSACTION_DESCRIPTION);
    }

    private String getOutputAddress(Transaction transaction) {
        return transaction.getOutputs().get(0).getAddress();
    }

    private Set<String> getInputAddresses(Transaction transaction) {
        return transaction.getInputs().stream().map(Input::getAddress).collect(toSet());
    }
}

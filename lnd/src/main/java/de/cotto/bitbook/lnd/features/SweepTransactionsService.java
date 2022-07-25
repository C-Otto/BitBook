package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.Input;
import de.cotto.bitbook.backend.model.Output;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.springframework.stereotype.Component;

import java.util.Set;

import static de.cotto.bitbook.backend.model.Chain.BTC;
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

    public long addFromSweeps(Set<TransactionHash> hashes) {
        Set<Transaction> sweepTransactions = transactionService.getTransactionDetails(hashes, BTC).stream()
                .filter(this::isSweepTransaction)
                .collect(toSet());
        sweepTransactions.forEach(transaction -> {
            addTransactionDescription(transaction);
            addAddressDescriptions(transaction);
            setAddressesAsOwned(transaction);
        });
        return sweepTransactions.size();
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private boolean isSweepTransaction(Transaction transaction) {
        if (transaction.getOutputs().size() == 1) {
            return true;
        }
        for (Input input : transaction.getInputs()) {
            for (Output output : transaction.getOutputs()) {
                if (input.getValue().equals(output.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addAddressDescriptions(Transaction transaction) {
        getInputAddresses(transaction)
                .forEach(address -> addressDescriptionService.set(address, DEFAULT_ADDRESS_DESCRIPTION));
        getOutputAddresses(transaction)
                .forEach(address -> addressDescriptionService.set(address, DEFAULT_ADDRESS_DESCRIPTION));
    }

    private void setAddressesAsOwned(Transaction transaction) {
        getInputAddresses(transaction).forEach(address -> addressOwnershipService.setAddressAsOwned(address, BTC));
        getOutputAddresses(transaction).forEach(address -> addressOwnershipService.setAddressAsOwned(address, BTC));
    }

    private void addTransactionDescription(Transaction transaction) {
        transactionDescriptionService.set(transaction.getHash(), SWEEP_TRANSACTION_DESCRIPTION);
    }

    private Set<Address> getOutputAddresses(Transaction transaction) {
        return transaction.getOutputs().stream().map(Output::getAddress).collect(toSet());
    }

    private Set<Address> getInputAddresses(Transaction transaction) {
        return transaction.getInputs().stream().map(Input::getAddress).collect(toSet());
    }
}

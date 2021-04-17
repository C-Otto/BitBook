package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SweepTransactionsService {
    private static final String SWEEP_TRANSACTION_DESCRIPTION = "lnd sweep transaction";
    private static final String DEFAULT_ADDRESS_DESCRIPTION = "lnd";

    private final Logger logger = LoggerFactory.getLogger(getClass());
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
        return hashes.stream()
                .map(this::getGetTransactionDetails)
                .filter(this::isSweepTransaction)
                .map(this::addTransactionDescription)
                .map(this::addAddressDescriptions)
                .map(this::setAddressesAsOwned)
                .count();
    }

    private Transaction getGetTransactionDetails(String transactionHash) {
        Transaction transactionDetails = transactionService.getTransactionDetails(transactionHash);
        if (transactionDetails.isInvalid()) {
            logger.warn("Unable to find transaction {}", transactionHash);
        }
        return transactionDetails;
    }

    private boolean isSweepTransaction(Transaction transaction) {
        return transaction.getOutputs().size() == 1;
    }

    private Transaction addAddressDescriptions(Transaction transaction) {
        addressDescriptionService.set(getInputAddress(transaction), DEFAULT_ADDRESS_DESCRIPTION);
        addressDescriptionService.set(getOutputAddress(transaction), DEFAULT_ADDRESS_DESCRIPTION);
        return transaction;
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    private Transaction setAddressesAsOwned(Transaction transaction) {
        addressOwnershipService.setAddressAsOwned(getInputAddress(transaction));
        addressOwnershipService.setAddressAsOwned(getOutputAddress(transaction));
        return transaction;
    }

    private Transaction addTransactionDescription(Transaction transaction) {
        transactionDescriptionService.set(transaction.getHash(), SWEEP_TRANSACTION_DESCRIPTION);
        return transaction;
    }

    private String getOutputAddress(Transaction transaction) {
        return transaction.getOutputs().get(0).getAddress();
    }

    private String getInputAddress(Transaction transaction) {
        return transaction.getInputs().get(0).getAddress();
    }
}

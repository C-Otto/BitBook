package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.InputOutput;
import de.cotto.bitbook.backend.model.Output;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.lnd.model.PoolLease;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.cotto.bitbook.ownership.OwnershipStatus.OWNED;

@Component
public class PoolLeasesService {
    private static final String DEFAULT_DESCRIPTION = "pool account";

    private final TransactionDescriptionService transactionDescriptionService;
    private final AddressDescriptionService addressDescriptionService;
    private final AddressOwnershipService addressOwnershipService;
    private final TransactionService transactionService;

    public PoolLeasesService(
            TransactionDescriptionService transactionDescriptionService,
            AddressDescriptionService addressDescriptionService,
            AddressOwnershipService addressOwnershipService,
            TransactionService transactionService
    ) {
        this.transactionDescriptionService = transactionDescriptionService;
        this.addressDescriptionService = addressDescriptionService;
        this.addressOwnershipService = addressOwnershipService;
        this.transactionService = transactionService;
    }

    public long addFromLeases(Set<PoolLease> leases) {
        Set<PoolLease> validLeases = leases.stream()
                .filter(lease -> transactionService.getTransactionDetails(lease.getTransactionHash()).isValid())
                .collect(Collectors.toSet());
        validLeases.forEach(lease -> {
            setTransactionDescription(lease);
            setChangeAddressDescriptionAndOwnership(lease);
            setChannelAddressDescriptionAndOwnership(lease);
        });
        return validLeases.size();
    }

    private void setTransactionDescription(PoolLease poolLease) {
        transactionDescriptionService.set(
                poolLease.getTransactionHash(),
                "Opening Channel with " + poolLease.getPubKey()
        );
    }

    private void setChannelAddressDescriptionAndOwnership(PoolLease poolLease) {
        String channelAddress = getChannelAddress(poolLease);
        addressDescriptionService.set(channelAddress, "Lightning Channel with " + poolLease.getPubKey());
        addressOwnershipService.setAddressAsOwned(channelAddress);
    }

    private void setChangeAddressDescriptionAndOwnership(PoolLease poolLease) {
        getChangeAddress(poolLease).ifPresent(changeAddress -> {
            Transaction transaction = transactionService.getTransactionDetails(poolLease.getTransactionHash());
            String description = transaction.getInputAddresses().stream()
                    .map(addressDescriptionService::getDescription)
                    .filter(inputDescription -> inputDescription.startsWith(DEFAULT_DESCRIPTION))
                    .findFirst()
                    .orElse(DEFAULT_DESCRIPTION);
            addressDescriptionService.set(changeAddress, description);
            addressOwnershipService.setAddressAsOwned(changeAddress);
        });
    }

    private String getChannelAddress(PoolLease poolLease) {
        Transaction transaction = transactionService.getTransactionDetails(poolLease.getTransactionHash());
        return getChannelOutput(poolLease, transaction).getAddress();
    }

    private Output getChannelOutput(PoolLease poolLease, Transaction transaction) {
        return transaction.getOutputs().get(poolLease.getOutputIndex());
    }

    private Optional<String> getChangeAddress(PoolLease poolLease) {
        Transaction transaction = transactionService.getTransactionDetails(poolLease.getTransactionHash());
        Coins ownedInputs = transaction.getInputs().stream()
                .filter(input -> OWNED.equals(addressOwnershipService.getOwnershipStatus(input.getAddress())))
                .map(InputOutput::getValue)
                .reduce(Coins.NONE, Coins::add);
        Coins channelAmount = getChannelOutput(poolLease, transaction).getValue();
        Coins premiumWithoutFees = poolLease.getPremiumWithoutFees();
        Coins expectedChange = ownedInputs
                .subtract(channelAmount)
                .add(premiumWithoutFees);
        return transaction.getOutputWithValue(expectedChange).map(Output::getAddress);
    }

}

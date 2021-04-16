package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.InputOutput;
import de.cotto.bitbook.backend.transaction.model.Output;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import de.cotto.bitbook.lnd.model.OnchainTransaction;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.cotto.bitbook.ownership.OwnershipStatus.OWNED;
import static de.cotto.bitbook.ownership.OwnershipStatus.UNKNOWN;
import static java.util.stream.Collectors.toList;

@Component
public class OnchainTransactionsService {
    private static final String DEFAULT_DESCRIPTION = "lnd";

    private final AddressOwnershipService addressOwnershipService;
    private final AddressDescriptionService addressDescriptionService;
    private final TransactionService transactionService;

    public OnchainTransactionsService(
            AddressOwnershipService addressOwnershipService,
            AddressDescriptionService addressDescriptionService,
            TransactionService transactionService
    ) {
        this.addressOwnershipService = addressOwnershipService;
        this.addressDescriptionService = addressDescriptionService;
        this.transactionService = transactionService;
    }

    public long addFromOnchainTransactions(Set<OnchainTransaction> onchainTransactions) {
        long oldResult;
        long result = -1;
        do {
            oldResult = result;
            result = runOnce(onchainTransactions);
        } while (result != oldResult);
        return result;
    }

    private long runOnce(Set<OnchainTransaction> onchainTransactions) {
        long result = 0;
        for (OnchainTransaction onchainTransaction : onchainTransactions) {
            result += handleFundingTransaction(onchainTransaction);
            result += handleOpeningTransaction(onchainTransaction);
        }
        return result;
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private long handleFundingTransaction(OnchainTransaction onchainTransaction) {
        Coins amount = onchainTransaction.getAmount();
        boolean hasFees = onchainTransaction.getFees().isPositive();
        boolean negativeAmount = amount.isNegative();
        boolean hasLabel = !onchainTransaction.getLabel().isBlank();
        if (hasFees || negativeAmount || hasLabel) {
            return 0;
        }
        Transaction transactionDetails =
                transactionService.getTransactionDetails(onchainTransaction.getTransactionHash());
        List<String> addressesWithAmount = getMatchingOutputAddresses(transactionDetails, amount);
        if (addressesWithAmount.size() == 1) {
            String address = addressesWithAmount.get(0);
            addressOwnershipService.setAddressAsOwned(address);
            addressDescriptionService.set(address, DEFAULT_DESCRIPTION);
            return 1;
        }
        return 0;
    }

    private long handleOpeningTransaction(OnchainTransaction onchainTransaction) {
        Coins amount = onchainTransaction.getAmount();
        boolean nonNegativeAmount = !amount.isNegative();
        boolean hasLabel = !onchainTransaction.getLabel().isBlank();
        if (nonNegativeAmount || hasLabel) {
            return 0;
        }
        Transaction transaction = transactionService.getTransactionDetails(onchainTransaction.getTransactionHash());
        if (hasUnownedInput(transaction) || hasMismatchedInputDescription(transaction)) {
            return 0;
        }
        Output channelOpenOutput = getChannelOpenOutput(transaction).orElse(null);
        if (channelOpenOutput == null || hasUnexpectedChannelCapacity(amount, transaction, channelOpenOutput)) {
            return 0;
        }
        setDescriptionAndOwnershipForOtherOutputs(transaction);
        return 1;
    }

    private void setDescriptionAndOwnershipForOtherOutputs(Transaction transactionDetails) {
        transactionDetails.getOutputs().stream()
                .map(InputOutput::getAddress)
                .filter(address -> UNKNOWN.equals(addressOwnershipService.getOwnershipStatus(address)))
                .forEach(address -> {
                    addressOwnershipService.setAddressAsOwned(address);
                    addressDescriptionService.set(address, DEFAULT_DESCRIPTION);
                });
    }

    private boolean hasUnexpectedChannelCapacity(
            Coins amount,
            Transaction transactionDetails,
            Output channelOpenOutput
    ) {
        Coins expectedChannelCapacity = amount.add(transactionDetails.getFees()).absolute();
        return !expectedChannelCapacity.equals(channelOpenOutput.getValue());
    }

    private Optional<Output> getChannelOpenOutput(Transaction transactionDetails) {
        return transactionDetails.getOutputs().stream()
                .filter(output -> addressDescriptionService.getDescription(output.getAddress())
                        .startsWith(ChannelsService.ADDRESS_DESCRIPTION_PREFIX))
                .findFirst();
    }

    private boolean hasMismatchedInputDescription(Transaction transactionDetails) {
        return transactionDetails.getInputs().stream()
                .map(InputOutput::getAddress)
                .map(addressDescriptionService::getDescription)
                .anyMatch(description -> !DEFAULT_DESCRIPTION.equals(description));
    }

    private boolean hasUnownedInput(Transaction transactionDetails) {
        return transactionDetails.getInputs().stream()
                .map(InputOutput::getAddress)
                .map(addressOwnershipService::getOwnershipStatus)
                .anyMatch(ownershipStatus -> !OWNED.equals(ownershipStatus));
    }

    private List<String> getMatchingOutputAddresses(Transaction transactionDetails, Coins amount) {
        return transactionDetails.getOutputs().stream()
                .filter(output -> amount.equals(output.getValue()))
                .map(Output::getAddress)
                .collect(toList());
    }
}

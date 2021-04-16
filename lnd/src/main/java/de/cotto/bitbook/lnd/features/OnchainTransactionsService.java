package de.cotto.bitbook.lnd.features;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
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
    private final TransactionDescriptionService transactionDescriptionService;

    public OnchainTransactionsService(
            AddressOwnershipService addressOwnershipService,
            AddressDescriptionService addressDescriptionService,
            TransactionService transactionService,
            TransactionDescriptionService transactionDescriptionService
    ) {
        this.addressOwnershipService = addressOwnershipService;
        this.addressDescriptionService = addressDescriptionService;
        this.transactionService = transactionService;
        this.transactionDescriptionService = transactionDescriptionService;
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
            result += handlePoolCreationTransaction(onchainTransaction);
        }
        return result;
    }

    private long handleFundingTransaction(OnchainTransaction onchainTransaction) {
        Coins amount = onchainTransaction.getAmount();
        if (onchainTransaction.hasFees() || amount.isNegative() || onchainTransaction.hasLabel()) {
            return 0;
        }
        Transaction transactionDetails =
                transactionService.getTransactionDetails(onchainTransaction.getTransactionHash());
        String address = getIfExactlyOne(getAddressForMatchingOutput(transactionDetails, amount)).orElse(null);
        if (address == null) {
            return 0;
        }
        setAddressAsOwnedWithDescription(address);
        return 1;
    }

    private long handleOpeningTransaction(OnchainTransaction onchainTransaction) {
        boolean nonNegativeAmount = onchainTransaction.getAmount().isNonNegative();
        if (nonNegativeAmount || onchainTransaction.hasLabel()) {
            return 0;
        }
        Transaction transaction = transactionService.getTransactionDetails(onchainTransaction.getTransactionHash());
        if (hasUnownedInput(transaction) || hasMismatchedInputDescription(transaction)) {
            return 0;
        }
        Coins expectedAmount = onchainTransaction.getAbsoluteAmountWithoutFees();
        Output channelOpenOutput = getChannelOpenOutput(transaction).orElse(null);
        if (channelOpenOutput == null || hasUnexpectedChannelCapacity(channelOpenOutput, expectedAmount)) {
            return 0;
        }
        addressOwnershipService.setAddressAsOwned(channelOpenOutput.getAddress());
        setDescriptionAndOwnershipForOtherOutputs(transaction, channelOpenOutput);
        return 1;
    }

    private void setDescriptionAndOwnershipForOtherOutputs(Transaction transactionDetails, Output channelOpenOutput) {
        transactionDetails.getOutputs().stream()
                .filter(output -> !channelOpenOutput.equals(output))
                .map(InputOutput::getAddress)
                .filter(address -> UNKNOWN.equals(addressOwnershipService.getOwnershipStatus(address)))
                .forEach(this::setAddressAsOwnedWithDescription);
    }

    private boolean hasUnexpectedChannelCapacity(Output channelOpenOutput, Coins absoluteAmountWithoutFees) {
        return !absoluteAmountWithoutFees.equals(channelOpenOutput.getValue());
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

    private long handlePoolCreationTransaction(OnchainTransaction onchainTransaction) {
        if (onchainTransaction.getAmount().isNonNegative()) {
            return 0;
        }
        if (!onchainTransaction.getLabel().startsWith(" poold -- AccountCreation(acct_key=")) {
            return 0;
        }
        Transaction transaction = transactionService.getTransactionDetails(onchainTransaction.getTransactionHash());
        Coins poolAmount = onchainTransaction.getAbsoluteAmountWithoutFees();
        String poolAddress = getIfExactlyOne(getAddressForMatchingOutput(transaction, poolAmount)).orElse(null);
        if (poolAddress == null) {
            return 0;
        }
        setForPoolAccountCreation(transaction, poolAddress, onchainTransaction.getLabel());
        return 1;
    }

    private void setForPoolAccountCreation(Transaction transaction, String poolAddress, String label) {
        String accountId = getAccountId(label);
        transactionDescriptionService.set(transaction.getHash(), "Creating pool account " + accountId);
        addressOwnershipService.setAddressAsOwned(poolAddress);
        addressDescriptionService.set(poolAddress, "pool account " + accountId);

        transaction.getInputs().stream()
                .map(InputOutput::getAddress)
                .forEach(this::setAddressAsOwnedWithDescription);
        transaction.getOutputs().stream()
                .map(InputOutput::getAddress)
                .filter(address -> !poolAddress.equals(address))
                .forEach(this::setAddressAsOwnedWithDescription);
    }

    private String getAccountId(String label) {
        return label.substring(label.indexOf('=') + 1, label.length() - 1);
    }

    private void setAddressAsOwnedWithDescription(String address) {
        addressOwnershipService.setAddressAsOwned(address);
        addressDescriptionService.set(address, DEFAULT_DESCRIPTION);
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private <T> Optional<T> getIfExactlyOne(List<T> list) {
        if (list.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }

    private List<String> getAddressForMatchingOutput(Transaction transactionDetails, Coins expectedValue) {
        return transactionDetails.getOutputs().stream()
                .filter(output -> expectedValue.equals(output.getValue()))
                .map(Output::getAddress)
                .collect(toList());
    }
}
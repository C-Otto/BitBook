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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.cotto.bitbook.ownership.OwnershipStatus.OWNED;
import static de.cotto.bitbook.ownership.OwnershipStatus.UNKNOWN;

@Component
public class OnchainTransactionsService extends AbstractTransactionsService {
    private static final String DEFAULT_DESCRIPTION = "lnd";

    private final SweepTransactionsService sweepTransactionsService;
    private final PoolTransactionService poolTransactionService;

    public OnchainTransactionsService(
            AddressOwnershipService addressOwnershipService,
            AddressDescriptionService addressDescriptionService,
            TransactionService transactionService,
            SweepTransactionsService sweepTransactionsService,
            PoolTransactionService poolTransactionService,
            TransactionDescriptionService transactionDescriptionService
    ) {
        super(addressOwnershipService, addressDescriptionService, transactionDescriptionService, transactionService);
        this.sweepTransactionsService = sweepTransactionsService;
        this.poolTransactionService = poolTransactionService;
    }

    public long addFromOnchainTransactions(Set<OnchainTransaction> onchainTransactions) {
        Map<OnchainTransaction, Long> oldSuccesses;
        Map<OnchainTransaction, Long> successes = new LinkedHashMap<>();
        do {
            oldSuccesses = successes;
            successes = runOnce(onchainTransactions);
        } while (!oldSuccesses.equals(successes));
        return successes.size();
    }

    private Map<OnchainTransaction, Long> runOnce(Set<OnchainTransaction> onchainTransactions) {
        Map<OnchainTransaction, Long> successes = new LinkedHashMap<>();
        for (OnchainTransaction onchainTransaction : onchainTransactions) {
            long result = 0;
            result += handleFundingTransaction(onchainTransaction);
            result += handleOpeningTransaction(onchainTransaction);
            result += handlePoolTransaction(onchainTransaction);
            result += handleSweepTransaction(onchainTransaction);
            result += handleSpendTransaction(onchainTransaction);
            if (result > 0) {
                successes.put(onchainTransaction, result);
            }
        }
        return successes;
    }

    private long handlePoolTransaction(OnchainTransaction onchainTransaction) {
        return poolTransactionService.addFromOnchainTransaction(onchainTransaction);
    }

    private long handleFundingTransaction(OnchainTransaction onchainTransaction) {
        Coins amount = onchainTransaction.getAmount();
        if (onchainTransaction.hasFees() || amount.isNegative() || onchainTransaction.hasLabel()) {
            return 0;
        }
        Transaction transaction =
                transactionService.getTransactionDetails(onchainTransaction.getTransactionHash());
        String address = transaction.getOutputWithValue(amount).map(InputOutput::getAddress).orElse(null);
        if (address == null) {
            return 0;
        }
        setAddressAsOwnedWithDescription(address);
        return 1;
    }

    private long handleOpeningTransaction(OnchainTransaction onchainTransaction) {
        boolean nonNegativeAmount = onchainTransaction.getAmount().isNonNegative();
        boolean notOpenChannelLabel = onchainTransaction.hasLabel()
                                      && !onchainTransaction.getLabel().startsWith("0:openchannel:");
        if (nonNegativeAmount || notOpenChannelLabel) {
            return 0;
        }
        String transactionHash = onchainTransaction.getTransactionHash();
        Transaction transaction = transactionService.getTransactionDetails(transactionHash);
        if (hasUnownedInput(transaction) || hasMismatchedInputDescription(transaction)) {
            return 0;
        }
        Coins expectedAmount = onchainTransaction.getAbsoluteAmountWithoutFees();
        Output channelOpenOutput = getChannelOpenOutput(transaction).orElse(null);
        if (channelOpenOutput == null || hasUnexpectedChannelCapacity(channelOpenOutput, expectedAmount)) {
            return 0;
        }
        addressOwnershipService.setAddressAsOwned(channelOpenOutput.getAddress());
        setInitiatorInTransactionDescription(transactionHash);
        setDescriptionAndOwnershipForOtherOutputs(transaction, channelOpenOutput);
        return 1;
    }

    private void setInitiatorInTransactionDescription(String transactionHash) {
        String expectedPrefix = "Opening Channel with ";
        String existingDescription = transactionDescriptionService.getDescription(transactionHash);
        if (existingDescription.startsWith(expectedPrefix) && existingDescription.endsWith(" (unknown)")) {
            String updatedDescription = existingDescription.replaceFirst(" \\(unknown\\)", " (local)");
            transactionDescriptionService.set(transactionHash, updatedDescription);
        }
    }

    private void setDescriptionAndOwnershipForOtherOutputs(Transaction transaction, Output outputToSkip) {
        transaction.getOutputs().stream()
                .filter(output -> !outputToSkip.equals(output))
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
        return transactionDetails.getInputAddresses().stream()
                .map(addressDescriptionService::getDescription)
                .anyMatch(description -> !DEFAULT_DESCRIPTION.equals(description));
    }

    private boolean hasUnownedInput(Transaction transactionDetails) {
        return transactionDetails.getInputAddresses().stream()
                .map(addressOwnershipService::getOwnershipStatus)
                .anyMatch(ownershipStatus -> !OWNED.equals(ownershipStatus));
    }

    private long handleSweepTransaction(OnchainTransaction onchainTransaction) {
        boolean amountMatchesFee = onchainTransaction.getAmount().absolute().equals(onchainTransaction.getFees());
        boolean unexpectedLabel = onchainTransaction.hasLabel()
                                  && !onchainTransaction.getLabel().startsWith("0:sweep:");
        if (unexpectedLabel || !amountMatchesFee || !onchainTransaction.hasFees()) {
            return 0;
        }
        return sweepTransactionsService.addFromSweeps(Set.of(onchainTransaction.getTransactionHash()));
    }

    private long handleSpendTransaction(OnchainTransaction onchainTransaction) {
        Coins amount = onchainTransaction.getAmount();
        if (onchainTransaction.hasLabel() || !onchainTransaction.hasFees() || amount.isNonNegative()) {
            return 0;
        }
        Transaction transaction = transactionService.getTransactionDetails(onchainTransaction.getTransactionHash());
        boolean unownedInput = transaction.getInputAddresses().stream()
                .anyMatch(address -> !OWNED.equals(addressOwnershipService.getOwnershipStatus(address)));
        if (unownedInput) {
            return 0;
        }
        Coins expectedOutputAmount = onchainTransaction.getAbsoluteAmountWithoutFees();
        Output targetOutput = transaction.getOutputWithValue(expectedOutputAmount).orElse(null);
        if (targetOutput == null) {
            return 0;
        }
        setDescriptionAndOwnershipForOtherOutputs(transaction, targetOutput);
        return 1;
    }
}

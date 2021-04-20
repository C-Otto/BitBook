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
import java.util.regex.Pattern;

@Component
public class PoolTransactionService extends AbstractTransactionsService {
    private static final Pattern POOL_ACCOUNT_CLOSE_PATTERN = Pattern.compile(
            " poold -- AccountModification\\(acct_key=[0-9a-f]*, expiry=[falsetru]+, deposit=false, is_close=true\\)"
    );
    private static final Pattern POOL_ACCOUNT_DEPOSIT_PATTERN = Pattern.compile(
            " poold -- AccountModification\\(acct_key=[0-9a-f]*, expiry=false, deposit=true, is_close=false\\)"
    );

    public PoolTransactionService(
            TransactionService transactionService,
            AddressDescriptionService addressDescriptionService,
            TransactionDescriptionService transactionDescriptionService,
            AddressOwnershipService addressOwnershipService
    ) {
        super(addressOwnershipService, addressDescriptionService, transactionDescriptionService, transactionService);
    }

    public long addFromOnchainTransaction(OnchainTransaction onchainTransaction) {
        long result = 0;
        result += handlePoolCreationTransaction(onchainTransaction);
        result += handlePoolDepositTransaction(onchainTransaction);
        result += handlePoolCloseTransaction(onchainTransaction);
        return result;
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
        String poolAddress = transaction.getOutputWithValue(poolAmount).map(InputOutput::getAddress).orElse(null);
        if (poolAddress == null) {
            return 0;
        }
        setForPoolAccountCreation(transaction, poolAddress, onchainTransaction.getLabel());
        return 1;
    }

    private void setForPoolAccountCreation(Transaction transaction, String poolAddress, String label) {
        addForPoolCreationOrDeposit(transaction, poolAddress, label, "Creating pool account ");
    }

    private long handlePoolDepositTransaction(OnchainTransaction onchainTransaction) {
        if (onchainTransaction.getAmount().isNonNegative()) {
            return 0;
        }
        if (!POOL_ACCOUNT_DEPOSIT_PATTERN.matcher(onchainTransaction.getLabel()).matches()) {
            return 0;
        }
        Transaction transaction = transactionService.getTransactionDetails(onchainTransaction.getTransactionHash());
        Coins subtractedAmount = Coins.NONE.subtract(onchainTransaction.getAmount());
        Coins otherInputs = transaction.getInputs().stream()
                .filter(input -> !DEFAULT_DESCRIPTION.equals(
                        addressDescriptionService.getDescription(input.getAddress())
                ))
                .map(InputOutput::getValue)
                .reduce(Coins.NONE, Coins::add);
        Coins expectedAmount = subtractedAmount.add(otherInputs).subtract(transaction.getFees());
        String poolAddress = transaction.getOutputWithValue(expectedAmount).map(InputOutput::getAddress).orElse(null);
        if (poolAddress == null) {
            return 0;
        }
        setForPoolAccountDeposit(transaction, poolAddress, onchainTransaction.getLabel());
        return 1;
    }

    private void setForPoolAccountDeposit(Transaction transaction, String poolAddress, String label) {
        addForPoolCreationOrDeposit(transaction, poolAddress, label, "Deposit into pool account ");
    }

    private void addForPoolCreationOrDeposit(
            Transaction transaction,
            String poolAddress,
            String label,
            String descriptionPrefix
    ) {
        String accountId = getAccountId(label);
        transactionDescriptionService.set(transaction.getHash(), descriptionPrefix + accountId);
        addressOwnershipService.setAddressAsOwned(poolAddress);
        addressDescriptionService.set(poolAddress, "pool account " + accountId);

        transaction.getInputAddresses().forEach(this::setAddressAsOwnedWithDescription);
        transaction.getOutputAddresses().stream()
                .filter(address -> !poolAddress.equals(address))
                .forEach(this::setAddressAsOwnedWithDescription);
    }

    private long handlePoolCloseTransaction(OnchainTransaction onchainTransaction) {
        if (onchainTransaction.getAmount().isNonPositive() || onchainTransaction.hasFees()) {
            return 0;
        }
        if (!POOL_ACCOUNT_CLOSE_PATTERN.matcher(onchainTransaction.getLabel()).matches()) {
            return 0;
        }
        Transaction transaction = transactionService.getTransactionDetails(onchainTransaction.getTransactionHash());
        Output lndOutput = getIfExactlyOne(transaction.getOutputs()).orElse(null);
        Coins poolAmount = onchainTransaction.getAmount();
        if (lndOutput == null || !lndOutput.getValue().equals(poolAmount)) {
            return 0;
        }
        setForPoolAccountClose(transaction, onchainTransaction.getLabel());
        return 1;
    }

    private void setForPoolAccountClose(Transaction transaction, String label) {
        String accountId = getAccountId(label);
        transactionDescriptionService.set(transaction.getHash(), "Closing pool account " + accountId);
        transaction.getAllAddresses().forEach(addressOwnershipService::setAddressAsOwned);
        transaction.getInputAddresses()
                .forEach(address -> addressDescriptionService.set(address, "pool account " + accountId));
        transaction.getOutputAddresses()
                .forEach(address -> addressDescriptionService.set(address, DEFAULT_DESCRIPTION));
    }

    private String getAccountId(String label) {
        int start = label.indexOf('=') + 1;
        int poolAccountKeyLength = 66;
        return label.substring(start, start + poolAccountKeyLength);
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    protected <T> Optional<T> getIfExactlyOne(List<T> list) {
        if (list.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }
}

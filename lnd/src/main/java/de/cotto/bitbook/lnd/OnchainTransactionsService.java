package de.cotto.bitbook.lnd;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Output;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import de.cotto.bitbook.lnd.model.OnchainTransaction;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

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
        long result = 0;
        for (OnchainTransaction onchainTransaction : onchainTransactions) {
            result += handleFundingTransaction(onchainTransaction);
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

    private List<String> getMatchingOutputAddresses(Transaction transactionDetails, Coins amount) {
        return transactionDetails.getOutputs().stream()
                .filter(output -> amount.equals(output.getValue()))
                .map(Output::getAddress)
                .collect(toList());
    }
}

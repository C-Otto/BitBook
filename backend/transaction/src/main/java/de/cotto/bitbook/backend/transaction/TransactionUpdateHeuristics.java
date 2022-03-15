package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.request.RequestPriority;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static de.cotto.bitbook.backend.model.Chain.BTC;

@Component
public class TransactionUpdateHeuristics {
    private static final int ONE_HOUR = 6;

    private static final int MANY_TRANSACTIONS_AGE_LIMIT = 4 * ONE_HOUR;
    private static final int RECENT_TRANSACTIONS_AGE_LIMIT = 6 * ONE_HOUR;
    private static final int WITH_BALANCE_AGE_LIMIT = 8 * ONE_HOUR;
    private static final int EMPTY_BALANCE_AGE_LIMIT = 24 * ONE_HOUR;
    private static final int LIMITED_USE_AGE_LIMIT = 7 * 24 * ONE_HOUR;

    private static final int MANY_TRANSACTION_COUNT_LIMIT = 10;
    private static final int RECENT_TRANSACTION_DAY_LIMIT = 7;
    private static final String LND_SWEEP_TRANSACTION = "lnd sweep transaction";

    private final BlockHeightService blockHeightService;
    private final TransactionService transactionService;
    private final TransactionDao transactionDao;
    private final AddressDescriptionService addressDescriptionService;
    private final TransactionDescriptionService transactionDescriptionService;

    public TransactionUpdateHeuristics(
            BlockHeightService blockHeightService,
            TransactionService transactionService,
            TransactionDao transactionDao,
            AddressDescriptionService addressDescriptionService,
            TransactionDescriptionService transactionDescriptionService
    ) {
        this.blockHeightService = blockHeightService;
        this.transactionService = transactionService;
        this.transactionDao = transactionDao;
        this.addressDescriptionService = addressDescriptionService;
        this.transactionDescriptionService = transactionDescriptionService;
    }

    public boolean isRecentEnough(AddressTransactions addressTransactions) {
        int currentBlockHeight = blockHeightService.getBlockHeight(addressTransactions.chain());
        int age = currentBlockHeight - addressTransactions.lastCheckedAtBlockHeight();
        if (age <= MANY_TRANSACTIONS_AGE_LIMIT) {
            return true;
        }
        boolean hasManyTransactions = addressTransactions.transactionHashes().size() > MANY_TRANSACTION_COUNT_LIMIT;
        if (hasManyTransactions) {
            return false;
        }
        if (age <= RECENT_TRANSACTIONS_AGE_LIMIT) {
            return true;
        }
        boolean hasRecentTransaction = hasRecentTransaction(addressTransactions);
        if (hasRecentTransaction) {
            return false;
        }
        if (age <= WITH_BALANCE_AGE_LIMIT) {
            return true;
        }
        if (hasEmptyBalance(addressTransactions) && age <= EMPTY_BALANCE_AGE_LIMIT) {
            return true;
        }
        return isUsedLimitedUseAddress(addressTransactions) && age <= LIMITED_USE_AGE_LIMIT;
    }

    public AddressTransactionsRequest getRequestWithTweakedPriority(AddressTransactionsRequest request) {
        if (request.getPriority() == RequestPriority.STANDARD) {
            return AddressTransactionsRequest.create(request.getKey(), RequestPriority.MEDIUM);
        }
        return request;
    }

    private boolean hasRecentTransaction(AddressTransactions addressTransactions) {
        Chain chain = addressTransactions.chain();
        LocalDateTime cutoffDate = LocalDateTime.now(ZoneOffset.UTC)
                .minus(RECENT_TRANSACTION_DAY_LIMIT, ChronoUnit.DAYS);
        return addressTransactions.transactionHashes().stream()
                .map(transactionHash -> transactionDao.getTransaction(transactionHash, chain))
                .filter(Transaction::isValid)
                .map(Transaction::getTime)
                .anyMatch(dateTime -> dateTime.isAfter(cutoffDate));
    }

    private boolean hasEmptyBalance(AddressTransactions addressTransactions) {
        Address address = addressTransactions.address();
        Set<TransactionHash> transactionHashes = addressTransactions.transactionHashes();
        Chain chain = addressTransactions.chain();
        Coins balance = transactionService.getTransactionDetails(transactionHashes, chain).stream()
                .map(transactionDetails -> transactionDetails.getDifferenceForAddress(address))
                .reduce(Coins.NONE, Coins::add);
        return balance.equals(Coins.NONE);
    }

    private boolean isUsedLimitedUseAddress(AddressTransactions addressTransactions) {
        int numberOfHashes = addressTransactions.transactionHashes().size();
        if (numberOfHashes == 1 && containsSweepTransaction(addressTransactions)) {
            return true;
        }
        return numberOfHashes == 2 && addressDescriptionService.getDescription(addressTransactions.address())
                .startsWith("Lightning-Channel with");
    }

    private boolean containsSweepTransaction(AddressTransactions addressTransactions) {
        Chain chain = addressTransactions.chain();
        if (chain != BTC) {
            return false;
        }
        return addressTransactions.transactionHashes().stream()
                .filter(hash -> LND_SWEEP_TRANSACTION.equals(transactionDescriptionService.getDescription(hash)))
                .map(transactionHash -> transactionDao.getTransaction(transactionHash, chain))
                .anyMatch(transaction -> transaction.getInputAddresses().contains(addressTransactions.address()));

    }
}

package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.request.RequestPriority;
import de.cotto.bitbook.backend.request.ResultFuture;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.toSet;

@Component
public class AddressTransactionsService {
    private static final int BLOCKS_CONSIDERED_RECENT = 48;

    private final PrioritizingAddressTransactionsProvider addressTransactionsProvider;
    private final TransactionService transactionService;
    private final AddressTransactionsDao addressTransactionsDao;
    private final BlockHeightService blockHeightService;

    public AddressTransactionsService(
            PrioritizingAddressTransactionsProvider addressTransactionsProvider,
            TransactionService transactionService,
            AddressTransactionsDao addressTransactionsDao,
            BlockHeightService blockHeightService
    ) {
        this.addressTransactionsProvider = addressTransactionsProvider;
        this.transactionService = transactionService;
        this.addressTransactionsDao = addressTransactionsDao;
        this.blockHeightService = blockHeightService;
    }

    @Async
    public void requestTransactionsInBackground(String address) {
        ResultFuture.getOrElse(getTransactions(address, RequestPriority.LOWEST), AddressTransactions.UNKNOWN);
    }

    public Set<AddressTransactions> getTransactionsForAddresses(Set<String> addresses) {
        Set<Future<AddressTransactions>> futures = addresses.stream()
                .map(address -> getTransactions(address, RequestPriority.STANDARD))
                .collect(toSet());
        return futures.stream()
                .map(future -> ResultFuture.getOrElse(future, AddressTransactions.UNKNOWN))
                .collect(toSet());
    }

    public AddressTransactions getTransactions(String address) {
        return ResultFuture.getOrElse(getTransactions(address, RequestPriority.STANDARD), AddressTransactions.UNKNOWN);
    }

    private Future<AddressTransactions> getTransactions(String address, RequestPriority requestPriority) {
        int currentBlockHeight = blockHeightService.getBlockHeight();
        AddressTransactions persistedAddressTransactions = addressTransactionsDao.getAddressTransactions(address);
        if (isValid(persistedAddressTransactions)) {
            return getUpdatedIfNecessary(persistedAddressTransactions, currentBlockHeight, requestPriority);
        }
        TransactionsRequestKey transactionsRequestKey = new TransactionsRequestKey(address, currentBlockHeight);
        return getResultFuture(transactionsRequestKey, requestPriority);
    }

    private Future<AddressTransactions> getUpdatedIfNecessary(
            AddressTransactions addressTransactions,
            int currentBlockHeight,
            RequestPriority requestPriority
    ) {
        if (isRecentEnough(addressTransactions, currentBlockHeight)) {
            return CompletableFuture.completedFuture(addressTransactions);
        }
        TransactionsRequestKey transactionsRequestKey =
                new TransactionsRequestKey(addressTransactions, currentBlockHeight);
        return getResultFuture(transactionsRequestKey, requestPriority);
    }

    private Future<AddressTransactions> getResultFuture(
            TransactionsRequestKey transactionsRequestKey,
            RequestPriority requestPriority
    ) {
        AddressTransactionsRequest request =
                AddressTransactionsRequest.create(transactionsRequestKey, requestPriority);
        return addressTransactionsProvider.getAddressTransactions(request).getFuture()
                .thenApply(transactions -> {
                    requestTransactionDetailsAndPersist(transactions);
                    return transactions;
                });
    }

    private boolean isValid(AddressTransactions persistedAddressTransactions) {
        return persistedAddressTransactions.isValid();
    }

    private boolean isRecentEnough(AddressTransactions addressTransactions, int currentBlockHeight) {
        return addressTransactions.getLastCheckedAtBlockHeight() + BLOCKS_CONSIDERED_RECENT >= currentBlockHeight;
    }

    private void requestTransactionDetailsAndPersist(AddressTransactions addressTransactions) {
        if (addressTransactions.isValid()) {
            transactionService.requestInBackground(addressTransactions.getTransactionHashes());
            addressTransactionsDao.saveAddressTransactions(addressTransactions);
        }
    }
}

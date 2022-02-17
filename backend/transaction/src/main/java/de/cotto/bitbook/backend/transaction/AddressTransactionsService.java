package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.request.RequestPriority;
import de.cotto.bitbook.backend.request.ResultFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.toSet;

@Component
public class AddressTransactionsService {
    private final PrioritizingAddressTransactionsProvider addressTransactionsProvider;
    private final TransactionService transactionService;
    private final AddressTransactionsDao addressTransactionsDao;
    private final BlockHeightService blockHeightService;
    private final TransactionUpdateHeuristics transactionUpdateHeuristics;

    public AddressTransactionsService(
            PrioritizingAddressTransactionsProvider addressTransactionsProvider,
            TransactionService transactionService,
            AddressTransactionsDao addressTransactionsDao,
            BlockHeightService blockHeightService,
            TransactionUpdateHeuristics transactionUpdateHeuristics
    ) {
        this.addressTransactionsProvider = addressTransactionsProvider;
        this.transactionService = transactionService;
        this.addressTransactionsDao = addressTransactionsDao;
        this.blockHeightService = blockHeightService;
        this.transactionUpdateHeuristics = transactionUpdateHeuristics;
    }

    @Async
    public void requestTransactionsInBackground(Address address, Chain chain) {
        ResultFuture.getOrElse(
                getTransactions(address, chain, RequestPriority.LOWEST),
                AddressTransactions.unknown(chain)
        );
    }

    public Set<AddressTransactions> getTransactionsForAddresses(Set<Address> addresses, Chain chain) {
        Set<Future<AddressTransactions>> futures = addresses.stream()
                .map(address -> getTransactions(address, chain, RequestPriority.STANDARD))
                .collect(toSet());
        return futures.stream()
                .map(future -> ResultFuture.getOrElse(future, AddressTransactions.unknown(chain)))
                .collect(toSet());
    }

    public AddressTransactions getTransactions(Address address, Chain chain) {
        return ResultFuture.getOrElse(
                getTransactions(address, chain, RequestPriority.STANDARD),
                AddressTransactions.unknown(chain)
        );
    }

    private Future<AddressTransactions> getTransactions(Address address, Chain chain, RequestPriority requestPriority) {
        int currentBlockHeight = blockHeightService.getBlockHeight(chain);
        AddressTransactions persistedAddressTransactions =
                addressTransactionsDao.getAddressTransactions(address, chain);
        if (isValid(persistedAddressTransactions)) {
            return getUpdatedIfNecessary(persistedAddressTransactions, currentBlockHeight, requestPriority);
        }
        TransactionsRequestKey transactionsRequestKey = new TransactionsRequestKey(address, chain, currentBlockHeight);
        AddressTransactionsRequest request = AddressTransactionsRequest.create(transactionsRequestKey, requestPriority);
        return getResultFuture(request);
    }

    private Future<AddressTransactions> getUpdatedIfNecessary(
            AddressTransactions addressTransactions,
            int currentBlockHeight,
            RequestPriority requestPriority
    ) {
        if (transactionUpdateHeuristics.isRecentEnough(addressTransactions)) {
            return CompletableFuture.completedFuture(addressTransactions);
        }
        TransactionsRequestKey transactionsRequestKey =
                new TransactionsRequestKey(addressTransactions, currentBlockHeight);
        AddressTransactionsRequest request = AddressTransactionsRequest.create(transactionsRequestKey, requestPriority);
        AddressTransactionsRequest tweakedRequest = transactionUpdateHeuristics.getRequestWithTweakedPriority(request);
        return getResultFuture(tweakedRequest);
    }

    private Future<AddressTransactions> getResultFuture(AddressTransactionsRequest request) {
        return addressTransactionsProvider.getAddressTransactions(request).getFuture()
                .thenApply(transactions -> {
                    requestTransactionDetailsAndPersist(transactions);
                    return transactions;
                });
    }

    private boolean isValid(AddressTransactions persistedAddressTransactions) {
        return persistedAddressTransactions.isValid();
    }

    private void requestTransactionDetailsAndPersist(AddressTransactions addressTransactions) {
        if (addressTransactions.isValid()) {
            transactionService.requestInBackground(
                    addressTransactions.getTransactionHashes(),
                    addressTransactions.getChain()
            );
            addressTransactionsDao.saveAddressTransactions(addressTransactions);
        }
    }
}

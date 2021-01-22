package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.request.RequestPriority;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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
        getTransactions(address, RequestPriority.LOWEST);
    }

    public AddressTransactions getTransactions(String address) {
        return getTransactions(address, RequestPriority.STANDARD);
    }

    private AddressTransactions getTransactions(String address, RequestPriority requestPriority) {
        int currentBlockHeight = blockHeightService.getBlockHeight();
        AddressTransactions persistedAddressTransactions = addressTransactionsDao.getAddressTransactions(address);
        if (isValid(persistedAddressTransactions)) {
            return getUpdatedIfNecessary(persistedAddressTransactions, currentBlockHeight, requestPriority);
        }
        TransactionsRequestKey transactionsRequestKey = new TransactionsRequestKey(address, currentBlockHeight);
        AddressTransactionsRequest request =
                AddressTransactionsRequest.create(transactionsRequestKey, requestPriority)
                        .getWithResultConsumer(this::requestTransactionDetailsAndPersist);
        return addressTransactionsProvider.getAddressTransactions(request);
    }

    private AddressTransactions getUpdatedIfNecessary(
            AddressTransactions addressTransactions,
            int currentBlockHeight,
            RequestPriority requestPriority
    ) {
        if (isRecentEnough(addressTransactions, currentBlockHeight)) {
            return addressTransactions;
        }
        TransactionsRequestKey transactionsRequestKey =
                new TransactionsRequestKey(addressTransactions, currentBlockHeight);
        AddressTransactionsRequest request =
                AddressTransactionsRequest.create(transactionsRequestKey, requestPriority)
                        .getWithResultConsumer(this::requestTransactionDetailsAndPersist);
        return addressTransactionsProvider.getAddressTransactions(request);
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

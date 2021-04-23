package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.request.RequestPriority;
import de.cotto.bitbook.backend.request.ResultFuture;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;
import static java.util.stream.Collectors.toSet;

@Component
public class TransactionService {
    /**
     * Only persist information about transactions that are that many blocks deep,
     * so that chain reorganization do not invalidate persisted data.
     */
    private static final int CONFIRMATION_LIMIT = 6;

    private final PrioritizingTransactionProvider prioritizingTransactionProvider;
    private final TransactionDao transactionDao;
    private final PriceService priceService;
    private final BlockHeightService blockHeightService;

    public TransactionService(
            PrioritizingTransactionProvider prioritizingTransactionProvider,
            TransactionDao transactionDao,
            PriceService priceService, BlockHeightService blockHeightService) {
        this.prioritizingTransactionProvider = prioritizingTransactionProvider;
        this.transactionDao = transactionDao;
        this.priceService = priceService;
        this.blockHeightService = blockHeightService;
    }

    public Set<Transaction> getTransactionDetails(Set<String> transactionHashes) {
        return getTransactionDetails(transactionHashes, STANDARD);
    }

    public Transaction getTransactionDetails(String transactionHash) {
        return getFromFuture(getTransactionDetails(transactionHash, STANDARD));
    }

    private Future<Transaction> getTransactionDetails(String transactionHash, RequestPriority requestPriority) {
        return getFromPersistenceOrDownload(transactionHash, requestPriority);
    }

    private Set<Transaction> getTransactionDetails(Set<String> transactionHashes, RequestPriority requestPriority) {
        Set<Future<Transaction>> futures = transactionHashes.stream()
                .map(transactionHash -> getTransactionDetails(transactionHash, requestPriority))
                .collect(toSet());
        return futures.stream().map(this::getFromFuture).collect(toSet());
    }

    @Async
    public void requestInBackground(Set<String> transactionHashes) {
        getTransactionDetails(transactionHashes, LOWEST);
    }

    private Future<Transaction> getFromPersistenceOrDownload(String transactionHash, RequestPriority requestPriority) {
        Transaction persistedTransaction = transactionDao.getTransaction(transactionHash);
        if (persistedTransaction.isValid()) {
            triggerPriceRequest(persistedTransaction);
            return CompletableFuture.completedFuture(persistedTransaction);
        }
        return downloadAndPersist(transactionHash, requestPriority);
    }

    private Future<Transaction> downloadAndPersist(String transactionHash, RequestPriority requestPriority) {
        TransactionRequest requestWithResultConsumer =
                new TransactionRequest(transactionHash, requestPriority);
        return prioritizingTransactionProvider.getTransaction(requestWithResultConsumer).getFuture()
                .thenApply(transaction -> {
                    if (isInvalidOrTooRecent(transaction)) {
                        return Transaction.UNKNOWN;
                    }
                    persistAndRequestPrice(transaction);
                    return transaction;
                });
    }

    private boolean isInvalidOrTooRecent(Transaction transaction) {
        return transaction.isInvalid() || hasInsufficientConfirmationDepth(transaction);
    }

    private boolean hasInsufficientConfirmationDepth(Transaction transaction) {
        int blockHeight = transaction.getBlockHeight();
        if (blockHeight <= 0) {
            // unconfirmed transaction, in mempool
            return true;
        }
        // too recent, may get lost in chain reorganization
        return blockHeight > getChainBlockHeight() - CONFIRMATION_LIMIT;
    }

    private void persistAndRequestPrice(Transaction transaction) {
        transactionDao.saveTransaction(transaction);
        triggerPriceRequest(transaction);
    }

    private void triggerPriceRequest(Transaction transaction) {
        priceService.requestPriceInBackground(transaction.getTime());
    }

    private int getChainBlockHeight() {
        return blockHeightService.getBlockHeight();
    }

    private Transaction getFromFuture(Future<Transaction> future) {
        return ResultFuture.getOrElse(future, Transaction.UNKNOWN);
    }
}

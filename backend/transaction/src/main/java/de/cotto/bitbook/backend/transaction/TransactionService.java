package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.request.RequestPriority;
import de.cotto.bitbook.backend.request.ResultFuture;
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

    public Set<Transaction> getTransactionDetails(Set<TransactionHash> transactionHashes, Chain chain) {
        return getTransactionDetails(transactionHashes, chain, STANDARD);
    }

    public Transaction getTransactionDetails(TransactionHash transactionHash, Chain chain) {
        return getFromFuture(getTransactionDetails(transactionHash, chain, STANDARD), chain);
    }

    private Future<Transaction> getTransactionDetails(
            TransactionHash transactionHash,
            Chain chain,
            RequestPriority requestPriority
    ) {
        return getFromPersistenceOrDownload(transactionHash, chain, requestPriority);
    }

    private Set<Transaction> getTransactionDetails(
            Set<TransactionHash> transactionHashes,
            Chain chain,
            RequestPriority requestPriority
    ) {
        Set<Future<Transaction>> futures = transactionHashes.stream()
                .map(transactionHash -> getTransactionDetails(transactionHash, chain, requestPriority))
                .collect(toSet());
        return futures.stream()
                .map(future -> getFromFuture(future, chain))
                .collect(toSet());
    }

    @Async
    public void requestInBackground(Set<TransactionHash> transactionHashes, Chain chain) {
        getTransactionDetails(transactionHashes, chain, LOWEST);
    }

    private Future<Transaction> getFromPersistenceOrDownload(
            TransactionHash transactionHash,
            Chain chain,
            RequestPriority requestPriority
    ) {
        Transaction persistedTransaction = transactionDao.getTransaction(transactionHash, chain);
        if (persistedTransaction.isValid()) {
            triggerPriceRequest(persistedTransaction);
            return CompletableFuture.completedFuture(persistedTransaction);
        }
        return downloadAndPersist(transactionHash, chain, requestPriority);
    }

    private Future<Transaction> downloadAndPersist(
            TransactionHash transactionHash,
            Chain chain,
            RequestPriority requestPriority
    ) {
        TransactionRequest request = new TransactionRequest(transactionHash, chain, requestPriority);
        return prioritizingTransactionProvider.getTransaction(request).getFuture()
                .thenApply(transaction -> {
                    if (isInvalidOrTooRecent(transaction)) {
                        return Transaction.unknown(transaction.getChain());
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
        return blockHeight > getChainBlockHeight(transaction.getChain()) - CONFIRMATION_LIMIT;
    }

    private void persistAndRequestPrice(Transaction transaction) {
        transactionDao.saveTransaction(transaction);
        triggerPriceRequest(transaction);
    }

    private void triggerPriceRequest(Transaction transaction) {
        priceService.requestPriceInBackground(transaction.getTime(), transaction.getChain());
    }

    private int getChainBlockHeight(Chain chain) {
        return blockHeightService.getBlockHeight(chain);
    }

    private Transaction getFromFuture(Future<Transaction> future, Chain chain) {
        return ResultFuture.getOrElse(future, Transaction.unknown(chain));
    }
}

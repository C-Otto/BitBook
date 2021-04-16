package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.request.RequestPriority;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static de.cotto.bitbook.backend.request.RequestPriority.LOWEST;
import static de.cotto.bitbook.backend.request.RequestPriority.STANDARD;

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
    private final Logger logger = LoggerFactory.getLogger(getClass());

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
        ForkJoinPool forkJoinPool = new ForkJoinPool(200);
        try {
            return forkJoinPool.submit(() ->
                    transactionHashes.parallelStream().map(this::getTransactionDetails).collect(Collectors.toSet())
            ).get();
        } catch (InterruptedException e) {
            logger.warn("Interrupted while getting transaction details", e);
            return Set.of(Transaction.UNKNOWN);
        } catch (ExecutionException e) {
            logger.warn("Exception while getting transaction details", e);
            return Set.of(Transaction.UNKNOWN);
        }
    }

    public Transaction getTransactionDetails(String transactionHash) {
        return getTransactionDetails(transactionHash, STANDARD);
    }

    public Transaction getTransactionDetails(String transactionHash, RequestPriority requestPriority) {
        Transaction transaction = getFromPersistenceOrDownload(transactionHash, requestPriority);
        triggerPriceRequest(transaction);
        return transaction;
    }

    @Async
    public void requestInBackground(Set<String> transactionHashes) {
        transactionHashes.parallelStream()
                .forEach(transactionHash -> getTransactionDetails(transactionHash, LOWEST));
    }

    private Transaction getFromPersistenceOrDownload(String transactionHash, RequestPriority requestPriority) {
        Transaction persistedTransaction = transactionDao.getTransaction(transactionHash);
        if (persistedTransaction.isValid()) {
            return persistedTransaction;
        }
        return downloadAndPersist(transactionHash, requestPriority);
    }

    private Transaction downloadAndPersist(String transactionHash, RequestPriority requestPriority) {
        TransactionRequest requestWithResultConsumer =
                new TransactionRequest(transactionHash, requestPriority, this::persistAndRequestPrice);
        Transaction transaction = prioritizingTransactionProvider.getTransaction(requestWithResultConsumer);
        if (isInvalidOrTooRecent(transaction)) {
            return Transaction.UNKNOWN;
        }
        return transaction;
    }

    private void persistAndRequestPrice(Transaction transaction) {
        if (isInvalidOrTooRecent(transaction)) {
            return;
        }
        transactionDao.saveTransaction(transaction);
        triggerPriceRequest(transaction);
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

    private void triggerPriceRequest(Transaction transaction) {
        if (transaction.isValid()) {
            priceService.requestPriceInBackground(transaction.getTime());
        }
    }

    private int getChainBlockHeight() {
        return blockHeightService.getBlockHeight();
    }
}

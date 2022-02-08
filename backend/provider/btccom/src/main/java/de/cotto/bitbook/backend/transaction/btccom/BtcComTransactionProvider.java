package de.cotto.bitbook.backend.transaction.btccom;

import de.cotto.bitbook.backend.model.Provider;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.deserialization.TransactionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BtcComTransactionProvider implements Provider<TransactionHash, Transaction> {
    private final BtcComClient btcComClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BtcComTransactionProvider(BtcComClient btcComClient) {
        this.btcComClient = btcComClient;
    }

    @Override
    public String getName() {
        return "BtcComTransactionProvider";
    }

    @Override
    public Optional<Transaction> get(TransactionHash transactionHash) {
        return getTransactionFromApi(transactionHash)
                .map(TransactionDto::toModel);
    }

    private Optional<BtcComTransactionDto> getTransactionFromApi(TransactionHash transactionHash) {
        logger.debug("Contacting btc.com API for hash {}", transactionHash);
        return btcComClient.getTransaction(transactionHash);
    }
}

package de.cotto.bitbook.backend.transaction.smartbit;

import de.cotto.bitbook.backend.Provider;
import de.cotto.bitbook.backend.transaction.deserialization.TransactionDto;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SmartbitTransactionProvider implements Provider<String, Transaction> {
    private final SmartbitClient smartbitClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public SmartbitTransactionProvider(SmartbitClient smartbitClient) {
        this.smartbitClient = smartbitClient;
    }

    @Override
    public Optional<Transaction> get(String transactionHash) {
        return getTransactionFromApi(transactionHash).map(TransactionDto::toModel);
    }

    @Override
    public String getName() {
        return "SmartbitTransactionProvider";
    }

    private Optional<SmartbitTransactionDto> getTransactionFromApi(String transactionHash) {
        logger.debug("Contacting Smartbit API for hash {}", transactionHash);
        return smartbitClient.getTransaction(transactionHash);
    }
}

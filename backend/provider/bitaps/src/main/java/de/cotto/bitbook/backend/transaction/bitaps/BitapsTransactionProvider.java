package de.cotto.bitbook.backend.transaction.bitaps;

import de.cotto.bitbook.backend.Provider;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.transaction.deserialization.TransactionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BitapsTransactionProvider implements Provider<String, Transaction> {
    private final BitapsClient bitapsClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BitapsTransactionProvider(BitapsClient bitapsClient) {
        this.bitapsClient = bitapsClient;
    }

    @Override
    public String getName() {
        return "BitapsTransactionProvider";
    }

    @Override
    public Optional<Transaction> get(String transactionHash) {
        return getTransactionFromApi(transactionHash).map(TransactionDto::toModel);
    }

    private Optional<BitapsTransactionDto> getTransactionFromApi(String transactionHash) {
        logger.debug("Contacting Bitaps API for hash {}", transactionHash);
        return bitapsClient.getTransaction(transactionHash);
    }
}

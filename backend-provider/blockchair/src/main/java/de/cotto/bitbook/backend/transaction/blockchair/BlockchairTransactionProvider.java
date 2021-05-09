package de.cotto.bitbook.backend.transaction.blockchair;

import de.cotto.bitbook.backend.Provider;
import de.cotto.bitbook.backend.transaction.deserialization.TransactionDto;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BlockchairTransactionProvider implements Provider<String, Transaction> {
    private final BlockchairClient blockchairClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BlockchairTransactionProvider(BlockchairClient blockchairClient) {
        this.blockchairClient = blockchairClient;
    }

    @Override
    public String getName() {
        return "BlockchairTransactionProvider";
    }

    @Override
    public Optional<Transaction> get(String transactionHash) {
        return getTransactionFromApi(transactionHash)
                .map(TransactionDto::toModel);
    }

    private Optional<BlockchairTransactionDto> getTransactionFromApi(String transactionHash) {
        logger.debug("Contacting Blockchair API for hash {}", transactionHash);
        return blockchairClient.getTransaction(transactionHash);
    }
}

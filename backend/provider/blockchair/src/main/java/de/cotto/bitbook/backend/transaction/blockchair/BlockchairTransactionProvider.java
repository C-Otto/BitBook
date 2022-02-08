package de.cotto.bitbook.backend.transaction.blockchair;

import de.cotto.bitbook.backend.model.Provider;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.deserialization.TransactionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BlockchairTransactionProvider implements Provider<TransactionHash, Transaction> {
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
    public Optional<Transaction> get(TransactionHash transactionHash) {
        return getTransactionFromApi(transactionHash)
                .map(TransactionDto::toModel);
    }

    private Optional<BlockchairTransactionDto> getTransactionFromApi(TransactionHash transactionHash) {
        logger.debug("Contacting Blockchair API for hash {}", transactionHash);
        return blockchairClient.getTransaction(transactionHash);
    }
}

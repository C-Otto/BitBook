package de.cotto.bitbook.backend.transaction.blockcypher;

import de.cotto.bitbook.backend.Provider;
import de.cotto.bitbook.backend.transaction.deserialization.TransactionDto;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BlockcypherTransactionProvider implements Provider<String, Transaction> {
    private final BlockcypherClient blockcypherClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BlockcypherTransactionProvider(BlockcypherClient blockcypherClient) {
        this.blockcypherClient = blockcypherClient;
    }

    @Override
    public Optional<Transaction> get(String transactionHash) {
        return getTransactionFromApi(transactionHash)
                .map(TransactionDto::toModel);
    }

    @Override
    public String getName() {
        return "BlockcypherTransactionProvider";
    }

    private Optional<BlockcypherTransactionDto> getTransactionFromApi(String transactionHash) {
        logger.debug("Contacting Blockcypher API for hash {}", transactionHash);
        return blockcypherClient.getTransaction(transactionHash);
    }
}

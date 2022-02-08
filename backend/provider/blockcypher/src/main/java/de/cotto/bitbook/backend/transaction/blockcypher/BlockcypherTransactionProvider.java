package de.cotto.bitbook.backend.transaction.blockcypher;

import de.cotto.bitbook.backend.model.Provider;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.deserialization.TransactionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BlockcypherTransactionProvider implements Provider<TransactionHash, Transaction> {
    private final BlockcypherClient blockcypherClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BlockcypherTransactionProvider(BlockcypherClient blockcypherClient) {
        this.blockcypherClient = blockcypherClient;
    }

    @Override
    public Optional<Transaction> get(TransactionHash transactionHash) {
        return getTransactionFromApi(transactionHash)
                .map(TransactionDto::toModel);
    }

    @Override
    public String getName() {
        return "BlockcypherTransactionProvider";
    }

    private Optional<BlockcypherTransactionDto> getTransactionFromApi(TransactionHash transactionHash) {
        logger.debug("Contacting Blockcypher API for hash {}", transactionHash);
        return blockcypherClient.getTransaction(transactionHash);
    }
}

package de.cotto.bitbook.backend.transaction.blockchaininfo;

import de.cotto.bitbook.backend.model.Provider;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.deserialization.TransactionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BlockchainInfoTransactionProvider implements Provider<TransactionHash, Transaction> {
    private final BlockchainInfoClient blockchainInfoClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BlockchainInfoTransactionProvider(BlockchainInfoClient blockchainInfoClient) {
        this.blockchainInfoClient = blockchainInfoClient;
    }

    @Override
    public Optional<Transaction> get(TransactionHash transactionHash) {
        logger.debug("Contacting blockchain.info API for hash {}", transactionHash);
        return blockchainInfoClient.getTransaction(transactionHash)
                .map(TransactionDto::toModel);
    }

    @Override
    public String getName() {
        return "BlockchainInfoTransactionProvider";
    }
}

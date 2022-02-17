package de.cotto.bitbook.backend.transaction.blockchaininfo;

import de.cotto.bitbook.backend.model.HashAndChain;
import de.cotto.bitbook.backend.model.Provider;
import de.cotto.bitbook.backend.model.ProviderException;
import de.cotto.bitbook.backend.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.Chain.BTC;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BlockchainInfoTransactionProvider implements Provider<HashAndChain, Transaction> {
    private final BlockchainInfoClient blockchainInfoClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BlockchainInfoTransactionProvider(BlockchainInfoClient blockchainInfoClient) {
        this.blockchainInfoClient = blockchainInfoClient;
    }

    @Override
    public boolean isSupported(HashAndChain key) {
        return key.chain() == BTC;
    }

    @Override
    public Optional<Transaction> get(HashAndChain hashAndChain) throws ProviderException {
        throwIfUnsupported(hashAndChain);
        logger.debug("Contacting blockchain.info API for hash {}", hashAndChain.hash());
        return blockchainInfoClient.getTransaction(hashAndChain.hash())
                .map(dto -> dto.toModel(BTC));
    }

    @Override
    public String getName() {
        return "BlockchainInfoTransactionProvider";
    }
}

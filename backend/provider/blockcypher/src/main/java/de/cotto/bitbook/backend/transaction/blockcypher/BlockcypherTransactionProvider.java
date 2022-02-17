package de.cotto.bitbook.backend.transaction.blockcypher;

import de.cotto.bitbook.backend.model.HashAndChain;
import de.cotto.bitbook.backend.model.Provider;
import de.cotto.bitbook.backend.model.ProviderException;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.Chain.BTC;

@Component
@SuppressWarnings("CPD-START")
public class BlockcypherTransactionProvider implements Provider<HashAndChain, Transaction> {
    private final BlockcypherClient blockcypherClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BlockcypherTransactionProvider(BlockcypherClient blockcypherClient) {
        this.blockcypherClient = blockcypherClient;
    }

    @Override
    public boolean isSupported(HashAndChain key) {
        return key.chain() == BTC;
    }

    @Override
    public Optional<Transaction> get(HashAndChain hashAndChain) throws ProviderException {
        throwIfUnsupported(hashAndChain);
        return getTransactionFromApi(hashAndChain.hash())
                .map(dto -> dto.toModel(BTC));
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

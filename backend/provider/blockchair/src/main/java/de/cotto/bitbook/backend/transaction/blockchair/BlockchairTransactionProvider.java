package de.cotto.bitbook.backend.transaction.blockchair;

import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.HashAndChain;
import de.cotto.bitbook.backend.model.Provider;
import de.cotto.bitbook.backend.model.ProviderException;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BSV;
import static de.cotto.bitbook.backend.model.Chain.BTC;

@Component
public class BlockchairTransactionProvider implements Provider<HashAndChain, Transaction> {
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
    public boolean isSupported(HashAndChain key) {
        Chain chain = key.chain();
        return chain == BTC || chain == BCH || chain == BSV;
    }

    @Override
    public Optional<Transaction> get(HashAndChain hashAndChain) throws ProviderException {
        throwIfUnsupported(hashAndChain);
        return getTransactionFromApi(hashAndChain)
                .map(dto -> dto.toModel(hashAndChain.chain()));
    }

    private Optional<BlockchairTransactionDto> getTransactionFromApi(HashAndChain hashAndChain) {
        TransactionHash transactionHash = hashAndChain.hash();
        Chain chain = hashAndChain.chain();
        String chainName = BlockchairChainName.get(chain);
        logger.debug("Contacting Blockchair API for hash {} in chain {}", transactionHash, chain);
        return blockchairClient.getTransaction(chainName, transactionHash);
    }

}

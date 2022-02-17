package de.cotto.bitbook.backend.transaction.btccom;

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
public class BtcComTransactionProvider implements Provider<HashAndChain, Transaction> {
    private final BtcComClient btcComClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BtcComTransactionProvider(BtcComClient btcComClient) {
        this.btcComClient = btcComClient;
    }

    @Override
    public String getName() {
        return "BtcComTransactionProvider";
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

    private Optional<BtcComTransactionDto> getTransactionFromApi(TransactionHash transactionHash) {
        logger.debug("Contacting btc.com API for hash {}", transactionHash);
        return btcComClient.getTransaction(transactionHash);
    }
}

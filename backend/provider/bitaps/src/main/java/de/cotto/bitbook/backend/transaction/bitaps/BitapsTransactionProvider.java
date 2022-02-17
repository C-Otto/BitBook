package de.cotto.bitbook.backend.transaction.bitaps;

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
public class BitapsTransactionProvider implements Provider<HashAndChain, Transaction> {
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
    public boolean isSupported(HashAndChain key) {
        return key.chain() == BTC;
    }

    @Override
    public Optional<Transaction> get(HashAndChain hashAndChain) throws ProviderException {
        throwIfUnsupported(hashAndChain);
        return getTransactionFromApi(hashAndChain.hash())
                .map(dto -> dto.toModel(BTC));
    }

    private Optional<BitapsTransactionDto> getTransactionFromApi(TransactionHash transactionHash) {
        logger.debug("Contacting Bitaps API for hash {}", transactionHash);
        return bitapsClient.getTransaction(transactionHash);
    }
}

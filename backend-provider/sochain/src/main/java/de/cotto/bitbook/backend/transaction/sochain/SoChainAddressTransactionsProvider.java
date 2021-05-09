package de.cotto.bitbook.backend.transaction.sochain;

import de.cotto.bitbook.backend.transaction.SimpleAddressTransactionsProvider;
import de.cotto.bitbook.backend.transaction.TransactionsRequestKey;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SoChainAddressTransactionsProvider extends SimpleAddressTransactionsProvider {
    private final SoChainClient soChainClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public SoChainAddressTransactionsProvider(SoChainClient soChainClient) {
        super();
        this.soChainClient = soChainClient;
    }

    @Override
    public String getName() {
        return "SoChainAddressTransactionsProvider";
    }

    @Override
    protected Optional<AddressTransactions> getFromApi(TransactionsRequestKey transactionsRequestKey) {
        String address = transactionsRequestKey.getAddress();
        logger.debug("Contacting SoChain API for transactions for address {}", address);
        return soChainClient.getAddressDetails(address)
                .map(dto -> dto.toModel(transactionsRequestKey.getBlockHeight(), address));
    }
}

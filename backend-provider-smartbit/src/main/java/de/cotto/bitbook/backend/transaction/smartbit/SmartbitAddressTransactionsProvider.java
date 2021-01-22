package de.cotto.bitbook.backend.transaction.smartbit;

import de.cotto.bitbook.backend.transaction.SimpleAddressTransactionsProvider;
import de.cotto.bitbook.backend.transaction.TransactionsRequestKey;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SmartbitAddressTransactionsProvider extends SimpleAddressTransactionsProvider {
    private final SmartbitClient smartbitClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public SmartbitAddressTransactionsProvider(SmartbitClient smartbitClient) {
        super();
        this.smartbitClient = smartbitClient;
    }

    @Override
    public String getName() {
        return "SmartbitAddressTransactionsProvider";
    }

    @Override
    protected Optional<AddressTransactions> getFromApi(TransactionsRequestKey transactionsRequestKey) {
        String address = transactionsRequestKey.getAddress();
        logger.debug("Contacting Smartbit API for transactions for address {}", address);
        return smartbitClient.getAddressTransactions(address)
                .map(dto -> dto.toModel(transactionsRequestKey.getBlockHeight(), address));
    }
}

package de.cotto.bitbook.backend.transaction.btccom;

import de.cotto.bitbook.backend.transaction.SimpleAddressTransactionsProvider;
import de.cotto.bitbook.backend.transaction.TransactionsRequestKey;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BtcComAddressTransactionsProvider extends SimpleAddressTransactionsProvider {
    private final BtcComClient btcComClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BtcComAddressTransactionsProvider(BtcComClient btcComClient) {
        super();
        this.btcComClient = btcComClient;
    }

    @Override
    public String getName() {
        return "BtcComAddressTransactionsProvider";
    }

    @Override
    protected Optional<AddressTransactions> getFromApi(TransactionsRequestKey transactionsRequestKey) {
        String address = transactionsRequestKey.getAddress();
        logger.debug("Contacting btc.com API for transactions for address {}", address);
        return btcComClient.getAddressDetails(address)
                .map(dto -> dto.toModel(transactionsRequestKey.getBlockHeight(), address));
    }
}

package de.cotto.bitbook.backend.transaction.btccom;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.transaction.SimpleAddressTransactionsProvider;
import de.cotto.bitbook.backend.transaction.TransactionsRequestKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.Chain.BTC;

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
        Address address = transactionsRequestKey.getAddress();
        logger.debug("Contacting btc.com API for transactions for address {}", address);
        return btcComClient.getAddressDetails(address)
                .map(dto -> dto.toModel(transactionsRequestKey.getBlockHeight(), address, BTC));
    }
}

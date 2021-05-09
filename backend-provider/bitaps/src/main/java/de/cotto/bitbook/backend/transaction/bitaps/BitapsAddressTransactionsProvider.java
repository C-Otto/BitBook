package de.cotto.bitbook.backend.transaction.bitaps;

import de.cotto.bitbook.backend.transaction.SimpleAddressTransactionsProvider;
import de.cotto.bitbook.backend.transaction.TransactionsRequestKey;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BitapsAddressTransactionsProvider extends SimpleAddressTransactionsProvider {
    private final BitapsClient bitapsClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BitapsAddressTransactionsProvider(BitapsClient bitapsClient) {
        super();
        this.bitapsClient = bitapsClient;
    }

    @Override
    public String getName() {
        return "BitapsAddressTransactionsProvider";
    }

    @Override
    protected Optional<AddressTransactions> getFromApi(TransactionsRequestKey transactionsRequestKey) {
        String address = transactionsRequestKey.getAddress();
        logger.debug("Contacting Bitaps API for transactions for address {}", address);
        return bitapsClient.getAddressTransactions(address)
                .map(dto -> dto.toModel(transactionsRequestKey.getBlockHeight(), address));
    }
}

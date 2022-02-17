package de.cotto.bitbook.backend.transaction.bitaps;

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
        Address address = transactionsRequestKey.getAddress();
        logger.debug("Contacting Bitaps API for transactions for address {}", address);
        return bitapsClient.getAddressTransactions(address)
                .map(dto -> dto.toModel(transactionsRequestKey.getBlockHeight(), address, BTC));
    }
}

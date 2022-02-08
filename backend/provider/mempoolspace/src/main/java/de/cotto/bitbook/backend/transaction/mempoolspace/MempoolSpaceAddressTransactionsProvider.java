package de.cotto.bitbook.backend.transaction.mempoolspace;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.transaction.SimpleAddressTransactionsProvider;
import de.cotto.bitbook.backend.transaction.TransactionsRequestKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MempoolSpaceAddressTransactionsProvider extends SimpleAddressTransactionsProvider {
    private final MempoolSpaceClient mempoolSpaceClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MempoolSpaceAddressTransactionsProvider(MempoolSpaceClient mempoolSpaceClient) {
        super();
        this.mempoolSpaceClient = mempoolSpaceClient;
    }

    @Override
    public String getName() {
        return "MempoolSpaceAddressTransactionsProvider";
    }

    @Override
    protected Optional<AddressTransactions> getFromApi(TransactionsRequestKey transactionsRequestKey) {
        Address address = transactionsRequestKey.getAddress();
        logger.debug("Contacting mempool.space API for transactions for address {}", address);
        return mempoolSpaceClient.getAddressDetails(address)
                .map(dto -> dto.toModel(transactionsRequestKey.getBlockHeight(), address));
    }
}

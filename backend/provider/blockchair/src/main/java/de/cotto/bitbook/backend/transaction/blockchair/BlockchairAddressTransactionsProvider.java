package de.cotto.bitbook.backend.transaction.blockchair;

import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.transaction.SimpleAddressTransactionsProvider;
import de.cotto.bitbook.backend.transaction.TransactionsRequestKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BlockchairAddressTransactionsProvider extends SimpleAddressTransactionsProvider {
    private final BlockchairClient blockchairClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BlockchairAddressTransactionsProvider(BlockchairClient blockchairClient) {
        super();
        this.blockchairClient = blockchairClient;
    }

    @Override
    public String getName() {
        return "BlockchairAddressTransactionsProvider";
    }

    @Override
    protected Optional<AddressTransactions> getFromApi(TransactionsRequestKey transactionsRequestKey) {
        String address = transactionsRequestKey.getAddress();
        logger.debug("Contacting Blockchair API for transactions for address {}", address);
        return blockchairClient.getAddressDetails(address)
                .map(dto -> dto.toModel(transactionsRequestKey.getBlockHeight(), address));
    }
}

package de.cotto.bitbook.backend.transaction.blockstream;

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
public class BlockstreamAddressTransactionsProvider extends SimpleAddressTransactionsProvider {
    private final BlockstreamInfoClient blockstreamInfoClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BlockstreamAddressTransactionsProvider(BlockstreamInfoClient blockstreamInfoClient) {
        super();
        this.blockstreamInfoClient = blockstreamInfoClient;
    }

    @Override
    public String getName() {
        return "BlockstreamInfoAddressTransactionsProvider";
    }

    @Override
    protected Optional<AddressTransactions> getFromApi(TransactionsRequestKey transactionsRequestKey) {
        Address address = transactionsRequestKey.getAddress();
        logger.debug("Contacting blockstream.info API for transactions for address {}", address);
        return blockstreamInfoClient.getAddressDetails(address)
                .map(dto -> dto.toModel(transactionsRequestKey.getBlockHeight(), address, BTC));
    }
}

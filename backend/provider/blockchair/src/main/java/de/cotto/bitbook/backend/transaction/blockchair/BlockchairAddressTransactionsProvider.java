package de.cotto.bitbook.backend.transaction.blockchair;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.transaction.SimpleAddressTransactionsProvider;
import de.cotto.bitbook.backend.transaction.TransactionsRequestKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BSV;
import static de.cotto.bitbook.backend.model.Chain.BTC;

@Component
public class BlockchairAddressTransactionsProvider extends SimpleAddressTransactionsProvider {
    private final BlockchairClient blockchairClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public BlockchairAddressTransactionsProvider(BlockchairClient blockchairClient) {
        super();
        this.blockchairClient = blockchairClient;
    }

    @Override
    public boolean isSupported(TransactionsRequestKey key) {
        Chain chain = key.chain();
        return chain == BTC || chain == BCH || chain == BSV;
    }

    @Override
    public String getName() {
        return "BlockchairAddressTransactionsProvider";
    }

    @Override
    protected Optional<AddressTransactions> getFromApi(TransactionsRequestKey transactionsRequestKey) {
        Address address = transactionsRequestKey.address();
        Chain chain = transactionsRequestKey.chain();
        String chainName = BlockchairChainName.get(chain);
        logger.debug("Contacting Blockchair API for transactions for address {} in chain {}", address, chain);
        return blockchairClient.getAddressDetails(chainName, address)
                .map(dto -> dto.toModel(transactionsRequestKey.blockHeight(), address, chain));
    }
}

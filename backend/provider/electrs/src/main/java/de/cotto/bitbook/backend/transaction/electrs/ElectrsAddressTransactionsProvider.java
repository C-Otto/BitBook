package de.cotto.bitbook.backend.transaction.electrs;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.SimpleAddressTransactionsProvider;
import de.cotto.bitbook.backend.transaction.TransactionsRequestKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class ElectrsAddressTransactionsProvider extends SimpleAddressTransactionsProvider {
    private final ElectrsClient electrsClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ElectrsAddressTransactionsProvider(ElectrsClient electrsClient) {
        super();
        this.electrsClient = electrsClient;
    }

    @Override
    public String getName() {
        return "ElectrsAddressTransactionsProvider";
    }

    @Override
    protected Optional<AddressTransactions> getFromApi(TransactionsRequestKey transactionsRequestKey) {
        Address address = transactionsRequestKey.getAddress();
        logger.debug("Contacting Electrs for transactions for address {}", address);
        Set<TransactionHash> hashes = electrsClient.getTransactionHashes(address).orElse(null);
        if (hashes == null) {
            return Optional.empty();
        }
        return Optional.of(new AddressTransactions(address, hashes, transactionsRequestKey.getBlockHeight()));
    }
}

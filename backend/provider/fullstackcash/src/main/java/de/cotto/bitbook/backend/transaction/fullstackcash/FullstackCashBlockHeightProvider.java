package de.cotto.bitbook.backend.transaction.fullstackcash;

import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.ProviderException;
import de.cotto.bitbook.backend.transaction.BlockHeightProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FullstackCashBlockHeightProvider implements BlockHeightProvider {
    private final FullstackCashClient fullstackCashClient;

    public FullstackCashBlockHeightProvider(FullstackCashClient fullstackCashClient) {
        this.fullstackCashClient = fullstackCashClient;
    }

    @Override
    public String getName() {
        return "FullstackCashBlockHeightProvider";
    }

    @Override
    public Optional<Integer> get(Chain chain) throws ProviderException {
        throwIfUnsupported(chain);
        try {
            return Optional.of(Integer.parseInt(fullstackCashClient.getBlockHeight()));
        } catch (NumberFormatException exception) {
            throw new ProviderException(exception);
        }
    }

    @Override
    public boolean isSupported(Chain chain) {
        return chain == Chain.BCH;
    }
}

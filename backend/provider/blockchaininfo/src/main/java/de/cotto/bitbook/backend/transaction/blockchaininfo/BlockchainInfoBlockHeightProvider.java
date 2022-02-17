package de.cotto.bitbook.backend.transaction.blockchaininfo;

import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.ProviderException;
import de.cotto.bitbook.backend.transaction.BlockHeightProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.Chain.BTC;

@Component
public class BlockchainInfoBlockHeightProvider implements BlockHeightProvider {
    private final BlockchainInfoClient blockchainInfoClient;

    public BlockchainInfoBlockHeightProvider(BlockchainInfoClient blockchainInfoClient) {
        this.blockchainInfoClient = blockchainInfoClient;
    }

    @Override
    public String getName() {
        return "BlockchainInfoBlockHeightProvider";
    }

    @Override
    public Optional<Integer> get(Chain chain) throws ProviderException {
        throwIfUnsupported(chain);
        try {
            return Optional.of(Integer.parseInt(blockchainInfoClient.getBlockHeight()));
        } catch (NumberFormatException exception) {
            throw new ProviderException(exception);
        }
    }

    @Override
    public boolean isSupported(Chain chain) {
        return chain == BTC;
    }
}

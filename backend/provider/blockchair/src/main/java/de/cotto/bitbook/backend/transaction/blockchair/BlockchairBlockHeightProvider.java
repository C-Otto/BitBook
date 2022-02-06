package de.cotto.bitbook.backend.transaction.blockchair;

import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.ProviderException;
import de.cotto.bitbook.backend.transaction.BlockHeightProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.Chain.BTC;

@Component
@SuppressWarnings("CPD-START")
public class BlockchairBlockHeightProvider implements BlockHeightProvider {
    private final BlockchairClient blockchairClient;

    public BlockchairBlockHeightProvider(BlockchairClient blockchairClient) {
        this.blockchairClient = blockchairClient;
    }

    @Override
    public String getName() {
        return "BlockchairBlockHeightProvider";
    }

    @Override
    public Optional<Integer> get(Chain chain) throws ProviderException {
        throwIfUnsupported(chain);
        String chainName = getChainName(chain);
        BlockchairBlockHeightDto dto = blockchairClient.getBlockHeight(chainName).orElseThrow(ProviderException::new);
        return Optional.of(dto.getBlockHeight());
    }

    @Override
    public boolean isSupported(Chain chain) {
        return chain == BTC || chain == Chain.BCH;
    }

    private String getChainName(Chain chain) {
        //noinspection EnhancedSwitchMigration
        switch (chain) {
            case BTC:
                return "bitcoin";
            case BCH:
                return "bitcoin-cash";
            default:
                throw new IllegalStateException();
        }
    }
}

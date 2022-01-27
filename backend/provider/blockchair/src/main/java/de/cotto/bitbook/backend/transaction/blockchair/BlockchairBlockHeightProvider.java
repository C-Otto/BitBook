package de.cotto.bitbook.backend.transaction.blockchair;

import de.cotto.bitbook.backend.ProviderException;
import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.transaction.BlockHeightProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
        BlockchairBlockHeightDto dto = blockchairClient.getBlockHeight().orElseThrow(ProviderException::new);
        return Optional.of(dto.getBlockHeight());
    }

    @Override
    public boolean isSupported(Chain chain) {
        return chain == Chain.BTC;
    }

    private void throwIfUnsupported(Chain chain) throws ProviderException {
        if (!isSupported(chain)) {
            throw new ProviderException();
        }
    }
}

package de.cotto.bitbook.backend.transaction.bitaps;

import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.ProviderException;
import de.cotto.bitbook.backend.transaction.BlockHeightProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.Chain.BTC;

@Component
public class BitapsBlockHeightProvider implements BlockHeightProvider {
    private final BitapsClient bitapsClient;

    public BitapsBlockHeightProvider(BitapsClient bitapsClient) {
        this.bitapsClient = bitapsClient;
    }

    @Override
    public String getName() {
        return "BitapsBlockHeightProvider";
    }

    @Override
    public Optional<Integer> get(Chain chain) throws ProviderException {
        throwIfUnsupported(chain);
        BitapsBlockHeightDto dto = bitapsClient.getBlockHeight().orElseThrow(ProviderException::new);
        return Optional.of(dto.getBlockHeight());
    }

    @Override
    public boolean isSupported(Chain chain) {
        return chain == BTC;
    }
}

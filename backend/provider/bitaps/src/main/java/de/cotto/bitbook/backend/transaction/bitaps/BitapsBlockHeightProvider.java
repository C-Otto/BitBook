package de.cotto.bitbook.backend.transaction.bitaps;

import de.cotto.bitbook.backend.ProviderException;
import de.cotto.bitbook.backend.transaction.BlockHeightProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
    public Optional<Integer> get() throws ProviderException {
        BitapsBlockHeightDto dto = bitapsClient.getBlockHeight().orElse(null);
        if (dto == null) {
            throw new ProviderException();
        }
        return Optional.of(dto.getBlockHeight());
    }
}

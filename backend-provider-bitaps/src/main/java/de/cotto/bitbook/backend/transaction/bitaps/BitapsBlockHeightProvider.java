package de.cotto.bitbook.backend.transaction.bitaps;

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
    public Optional<Integer> get() {
        return bitapsClient.getBlockHeight().map(BitapsBlockHeightDto::getBlockHeight);
    }
}

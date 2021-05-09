package de.cotto.bitbook.backend.transaction.blockchaininfo;

import de.cotto.bitbook.backend.transaction.BlockHeightProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
    public Optional<Integer> get() {
        try {
            return Optional.of(Integer.parseInt(blockchainInfoClient.getBlockHeight()));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }
}

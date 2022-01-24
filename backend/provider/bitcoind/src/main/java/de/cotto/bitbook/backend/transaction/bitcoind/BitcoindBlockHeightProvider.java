package de.cotto.bitbook.backend.transaction.bitcoind;

import de.cotto.bitbook.backend.ProviderException;
import de.cotto.bitbook.backend.transaction.BlockHeightProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BitcoindBlockHeightProvider implements BlockHeightProvider {
    private final BitcoinCliWrapper bitcoinCliWrapper;

    public BitcoindBlockHeightProvider(BitcoinCliWrapper bitcoinCliWrapper) {
        this.bitcoinCliWrapper = bitcoinCliWrapper;
    }

    @Override
    public String getName() {
        return "BitcoindBlockHeightProvider";
    }

    @Override
    public Optional<Integer> get() throws ProviderException {
        int blockHeight = bitcoinCliWrapper.getBlockCount().orElseThrow(ProviderException::new);
        return Optional.of(blockHeight);
    }
}

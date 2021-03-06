package de.cotto.bitbook.backend.transaction.bitcoind;

import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.ProviderException;
import de.cotto.bitbook.backend.transaction.BlockHeightProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.cotto.bitbook.backend.model.Chain.BTC;

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
    public Optional<Integer> get(Chain chain) throws ProviderException {
        throwIfUnsupported(chain);
        int blockHeight = bitcoinCliWrapper.getBlockCount().orElseThrow(ProviderException::new);
        return Optional.of(blockHeight);
    }

    @Override
    public boolean isSupported(Chain chain) {
        return chain == BTC;
    }
}

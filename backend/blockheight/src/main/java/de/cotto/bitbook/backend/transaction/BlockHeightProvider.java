package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.Provider;
import de.cotto.bitbook.backend.ProviderException;

import java.util.Optional;

public interface BlockHeightProvider extends Provider<Object, Integer> {
    Optional<Integer> get() throws ProviderException;

    @Override
    default Optional<Integer> get(Object key) throws ProviderException {
        return get();
    }
}

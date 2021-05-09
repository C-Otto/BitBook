package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.Provider;

import java.util.Optional;

public interface BlockHeightProvider extends Provider<Object, Integer> {
    Optional<Integer> get();

    @Override
    default Optional<Integer> get(Object key) {
        return get();
    }
}

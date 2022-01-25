package de.cotto.bitbook.backend;

import java.util.Optional;

public interface Provider<K, R> {
    String getName();

    Optional<R> get(K key) throws ProviderException;

    default boolean isSupported(K key) {
        return true;
    }
}

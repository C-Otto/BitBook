package de.cotto.bitbook.backend.model;

import java.util.Optional;

public interface Provider<K, R> {
    String getName();

    Optional<R> get(K key) throws ProviderException;

    default boolean isSupported(K key) {
        return true;
    }

    default void throwIfUnsupported(K key) throws ProviderException {
        if (!isSupported(key)) {
            throw new ProviderException();
        }
    }
}

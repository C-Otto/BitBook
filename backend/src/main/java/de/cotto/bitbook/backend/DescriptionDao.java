package de.cotto.bitbook.backend;

import java.util.Set;

public interface DescriptionDao<K, T> {
    T get(K key);

    void save(T value);

    Set<T> findWithDescriptionInfix(String infix);

    void remove(K key);
}

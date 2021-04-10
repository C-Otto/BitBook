package de.cotto.bitbook.backend;

import java.util.Set;

public interface DescriptionDao<T> {
    T get(String key);

    void save(T value);

    Set<T> findWithDescriptionInfix(String infix);

    void remove(String key);
}

package de.cotto.bitbook.backend;

import de.cotto.bitbook.backend.model.StringWithDescription;

import java.util.Set;

public class DescriptionService<K, T extends StringWithDescription<T>> {
    private static final int MINIMUM_LENGTH_FOR_COMPLETION = 3;

    private final DescriptionDao<K, T> dao;

    public DescriptionService(DescriptionDao<K, T> dao) {
        this.dao = dao;
    }

    public T get(K key) {
        return dao.get(key);
    }

    public String getDescription(K key) {
        return get(key).getDescription();
    }

    protected void set(T stringWithDescription) {
        if (stringWithDescription.getDescription().isBlank()) {
            return;
        }
        dao.save(stringWithDescription);
    }

    public void remove(K key) {
        dao.remove(key);
    }

    public Set<T> getWithDescriptionInfix(String infix) {
        if (infix.length() < MINIMUM_LENGTH_FOR_COMPLETION) {
            return Set.of();
        }
        return dao.findWithDescriptionInfix(infix);
    }
}

package de.cotto.bitbook.backend.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TestableProvider implements Provider<String, Integer> {
    public final List<String> seenKeys = Collections.synchronizedList(new ArrayList<>());

    public TestableProvider() {
        // default constructor
    }

    @Override
    public boolean isSupported(String key) {
        if ("unsupported".equals(key)) {
            return false;
        }
        return Provider.super.isSupported(key);
    }

    @Override
    public Optional<Integer> get(String key) throws ProviderException {
        seenKeys.add(key);
        if (key == null) {
            return Optional.empty();
        }
        if ("".equals(key)) {
            return Optional.empty();
        }
        if ("wait".equals(key)) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        if ("providerException".equals(key)) {
            throw new ProviderException();
        }
        return Optional.of(key.length());
    }

    @Override
    public String getName() {
        return "TestableProvider";
    }
}

package de.cotto.bitbook.backend;

import de.cotto.bitbook.backend.model.AddressWithDescription;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AddressDescriptionService {
    private static final int MINIMUM_LENGTH_FOR_COMPLETION = 3;
    private final AddressWithDescriptionDao dao;

    public AddressDescriptionService(AddressWithDescriptionDao dao) {
        this.dao = dao;
    }

    public AddressWithDescription get(String address) {
        return dao.get(address);
    }

    public void set(String address, String description) {
        if (description.isBlank()) {
            return;
        }
        dao.save(new AddressWithDescription(address, description));
    }

    public void remove(String address) {
        dao.remove(address);
    }

    public Set<AddressWithDescription> getAddressesWithDescriptionInfix(String infix) {
        if (infix.length() < MINIMUM_LENGTH_FOR_COMPLETION) {
            return Set.of();
        }
        return dao.findWithDescriptionInfix(infix);
    }
}

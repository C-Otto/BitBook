package de.cotto.bitbook.backend;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import org.springframework.stereotype.Component;

@Component
public class AddressDescriptionService extends DescriptionService<Address, AddressWithDescription> {
    public AddressDescriptionService(AddressWithDescriptionDao dao) {
        super(dao);
    }

    public void set(Address address, String description) {
        set(new AddressWithDescription(address, description));
    }
}

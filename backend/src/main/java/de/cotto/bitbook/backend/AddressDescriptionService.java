package de.cotto.bitbook.backend;

import de.cotto.bitbook.backend.model.AddressWithDescription;
import org.springframework.stereotype.Component;

@Component
public class AddressDescriptionService extends DescriptionService<AddressWithDescription> {
    public AddressDescriptionService(AddressWithDescriptionDao dao) {
        super(dao);
    }

    public void set(String address, String description) {
        set(new AddressWithDescription(address, description));
    }
}

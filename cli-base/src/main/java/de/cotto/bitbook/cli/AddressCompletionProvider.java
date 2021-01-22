package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.transaction.AddressCompletionDao;
import org.springframework.stereotype.Component;

@Component
public class AddressCompletionProvider extends AbstractAddressCompletionProvider {

    public AddressCompletionProvider(
            AddressDescriptionService addressDescriptionService,
            AddressCompletionDao addressCompletionDao
    ) {
        super(addressDescriptionService, addressCompletionDao);
    }

    @Override
    protected boolean shouldConsider(AddressWithDescription addressWithDescription) {
        return true;
    }

}

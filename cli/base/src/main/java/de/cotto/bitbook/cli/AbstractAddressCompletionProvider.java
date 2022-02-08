package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.transaction.AddressCompletionDao;

import java.util.Set;
import java.util.function.Function;

public abstract class AbstractAddressCompletionProvider
        extends AbstractCompletionProvider<Address, AddressWithDescription> {
    private final AddressCompletionDao addressCompletionDao;

    public AbstractAddressCompletionProvider(
            AddressDescriptionService addressDescriptionService,
            AddressCompletionDao addressCompletionDao
    ) {
        super(addressDescriptionService);
        this.addressCompletionDao = addressCompletionDao;
    }

    @Override
    protected Set<Function<String, Set<Address>>> getStringCompleters() {
        return Set.of(
                addressCompletionDao::completeFromAddressTransactions,
                addressCompletionDao::completeFromInputsAndOutputs
        );
    }

    @Override
    protected boolean isTooShort(String input, int minimumLengthForCompletion) {
        if (input.startsWith("bc1")) {
            return input.length() < minimumLengthForCompletion + 3;
        }
        return input.length() < minimumLengthForCompletion;
    }
}

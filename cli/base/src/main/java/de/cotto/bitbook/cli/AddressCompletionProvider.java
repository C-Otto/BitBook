package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.transaction.AddressCompletionDao;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public Optional<String> completeIfUnique(String addressPrefix) {
        Set<String> proposals = completeUsingStringCompleters(getStringToComplete(addressPrefix))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        if (proposals.size() == 1) {
            return proposals.stream().findFirst();
        }
        return Optional.empty();
    }
}

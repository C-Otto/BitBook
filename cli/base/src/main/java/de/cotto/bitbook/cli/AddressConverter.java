package de.cotto.bitbook.cli;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

@Component
public class AddressConverter implements Converter<String, CliAddress> {
    private static final String ELLIPSIS = "…";
    private final AddressCompletionProvider addressCompletionProvider;

    public AddressConverter(AddressCompletionProvider addressCompletionProvider) {
        this.addressCompletionProvider = addressCompletionProvider;
    }

    @Override
    public CliAddress convert(@Nonnull String address) {
        if (address.endsWith(ELLIPSIS)) {
            Optional<String> completed = addressCompletionProvider.completeIfUnique(address);
            if (completed.isPresent()) {
                return new CliAddress(completed.get());
            }
        }
        return new CliAddress(address);
    }
}
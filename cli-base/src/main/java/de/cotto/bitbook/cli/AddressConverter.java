package de.cotto.bitbook.cli;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class AddressConverter implements Converter<String, CliAddress> {
    public AddressConverter() {
        // default constructor
    }

    @Override
    public CliAddress convert(@Nonnull String address) {
        return new CliAddress(address);
    }
}
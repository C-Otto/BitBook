package de.cotto.bitbook.cli;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class TransactionHashConverter implements Converter<String, CliTransactionHash> {
    public TransactionHashConverter() {
        // default constructor
    }

    @Override
    public CliTransactionHash convert(@Nonnull String transactionHash) {
        return new CliTransactionHash(transactionHash);
    }
}
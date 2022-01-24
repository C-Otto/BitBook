package de.cotto.bitbook.backend.transaction.bitcoind;

import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class BitcoinCliWrapper {

    public static final String COMMAND = "bitcoin-cli";
    public static final String GET_BLOCK_COUNT = "getblockcount";

    public BitcoinCliWrapper() {
        // default constructor
    }

    public Optional<Integer> getBlockCount() {
        String result = execute(COMMAND, GET_BLOCK_COUNT).orElse(null);
        if (result == null) {
            return Optional.empty();
        }
        String stripped = result.strip();
        try {
            return Optional.of(Integer.parseInt(stripped));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @VisibleForTesting
    protected Optional<String> execute(String... command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(command);
        try {
            Process process = processBuilder.start();
            byte[] bytes = process.getInputStream().readAllBytes();
            return Optional.of(new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}

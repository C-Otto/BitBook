package de.cotto.bitbook.lnd.cli;

import de.cotto.bitbook.lnd.PoolService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@ShellComponent
public class PoolCommands {
    private final PoolService poolService;

    public PoolCommands(PoolService poolService) {
        this.poolService = poolService;
    }

    @ShellMethod("Add information from pool leases information obtained by `pool auction leases`")
    public String poolAddFromLeases(File jsonFile) throws IOException {
        long numberOfLeases = poolService.addFromLeases(readFile(jsonFile));
        if (numberOfLeases == 0) {
            return "Unable to find leases in file";
        }
        return "Added information for " + numberOfLeases + " leases";
    }

    private String readFile(File jsonFile) throws IOException {
        return Files.readString(jsonFile.toPath(), StandardCharsets.US_ASCII);
    }
}

package de.cotto.bitbook.lnd.cli;

import de.cotto.bitbook.lnd.LndService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@ShellComponent
public class LndCommands {
    private final LndService lndService;

    public LndCommands(LndService lndService) {
        this.lndService = lndService;
    }

    @ShellMethod("Add information from lnd sweep information obtained by `lncli wallet listsweeps`")
    public String lndAddFromSweeps(File jsonFile) throws IOException {
        String content = Files.readString(jsonFile.toPath(), StandardCharsets.US_ASCII);
        long numberOfSweepTransactions = lndService.lndAddFromSweeps(content);
        if (numberOfSweepTransactions == 0) {
            return "Unable to find sweep transactions in file";
        }
        return "Added information for " + numberOfSweepTransactions + " sweep transactions";
    }
}

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
        long numberOfSweepTransactions = lndService.addFromSweeps(readFile(jsonFile));
        if (numberOfSweepTransactions == 0) {
            return "Unable to find sweep transactions in file";
        }
        return "Added information for " + numberOfSweepTransactions + " sweep transactions";
    }

    @ShellMethod("Add information from lnd unspent outputs obtained by `lncli listunspent`")
    public String lndAddFromUnspentOutputs(File jsonFile) throws IOException {
        long numberOfUnspentOutputs = lndService.addFromUnspentOutputs(readFile(jsonFile));
        if (numberOfUnspentOutputs == 0) {
            return "Unable to find unspent output address in file";
        }
        return "Marked " + numberOfUnspentOutputs + " addresses as owned by lnd";
    }

    @ShellMethod("Add information from channels obtained by `lncli listchannels`")
    public String lndAddFromChannels(File jsonFile) throws IOException {
        long channels = lndService.addFromChannels(readFile(jsonFile));
        if (channels == 0) {
            return "Unable to find channel in file";
        }
        return "Added information for %d channels".formatted(channels);
    }

    @ShellMethod("Add information from closed channels obtained by `lncli closedchannels`")
    public String lndAddFromClosedChannels(File jsonFile) throws IOException {
        long closedChannels = lndService.addFromClosedChannels(readFile(jsonFile));
        if (closedChannels == 0) {
            return "Unable to find closed channel in file";
        }
        return "Added information for %d closed channels".formatted(closedChannels);
    }

    @ShellMethod("Add information from on-chain transactions obtained by `lncli listchaintxns`")
    public String lndAddFromOnchainTransactions(File jsonFile) throws IOException {
        long numberOfTransactions = lndService.addFromOnchainTransactions(readFile(jsonFile));
        if (numberOfTransactions == 0) {
            return "Unable to find usable transactions in file";
        }
        return "Added information from %d transactions".formatted(numberOfTransactions);
    }

    private String readFile(File jsonFile) throws IOException {
        return Files.readString(jsonFile.toPath(), StandardCharsets.UTF_8);
    }
}

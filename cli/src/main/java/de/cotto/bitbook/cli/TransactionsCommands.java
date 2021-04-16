package de.cotto.bitbook.cli;

import com.google.common.base.Functions;
import com.google.common.collect.Sets;
import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.transaction.AddressTransactionsService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@ShellComponent
public class TransactionsCommands {

    private final TransactionService transactionService;
    private final AddressTransactionsService addressTransactionsService;
    private final AddressDescriptionService addressDescriptionService;
    private final TransactionDescriptionService transactionDescriptionService;
    private final TransactionFormatter transactionFormatter;
    private final AddressFormatter addressFormatter;
    private final PriceService priceService;

    public TransactionsCommands(
            TransactionService transactionService,
            AddressTransactionsService addressTransactionsService,
            AddressDescriptionService addressDescriptionService,
            TransactionDescriptionService transactionDescriptionService,
            TransactionFormatter transactionFormatter,
            AddressFormatter addressFormatter,
            PriceService priceService
    ) {
        this.transactionService = transactionService;
        this.addressTransactionsService = addressTransactionsService;
        this.addressDescriptionService = addressDescriptionService;
        this.transactionDescriptionService = transactionDescriptionService;
        this.transactionFormatter = transactionFormatter;
        this.addressFormatter = addressFormatter;
        this.priceService = priceService;
    }

    @ShellMethod("Get data for a given transaction")
    public String getTransactionDetails(
            @ShellOption(valueProvider = TransactionHashCompletionProvider.class) CliTransactionHash transactionHash
    ) {
        if (transactionHash.getTransactionHash().isBlank()) {
            return CliTransactionHash.ERROR_MESSAGE;
        }
        Transaction transaction = transactionService.getTransactionDetails(transactionHash.getTransactionHash());
        return transactionFormatter.format(transaction);
    }

    @ShellMethod("Get transactions for address")
    public String getAddressTransactions(
            @ShellOption(valueProvider = AddressCompletionProvider.class) CliAddress address
    ) {
        String addressString = address.getAddress();
        if (addressString.isEmpty()) {
            return "Expected base58 or bech32 address";
        }
        AddressTransactions transactions = addressTransactionsService.getTransactions(addressString);
        String description = addressDescriptionService.getDescription(addressString);
        StringBuilder result = new StringBuilder(51);
        result.append("Address: ").append(addressString)
                .append(' ')
                .append(addressFormatter.getFormattedOwnershipStatus(addressString))
                .append("\nDescription: ")
                .append(description);
        List<String> hashes = transactions.getTransactionHashes().stream().sorted().collect(toList());
        result.append("\nTransaction hashes (")
                .append(hashes.size())
                .append("):\n")
                .append(formattedHashesSortedByDifferenceForAddress(hashes, addressString));
        return StringUtils.stripEnd(result.toString(), "\n");
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    @ShellMethod("Sets a description for the transaction")
    public String setTransactionDescription(
            @ShellOption(valueProvider = TransactionHashCompletionProvider.class) CliTransactionHash transactionHash,
            @ShellOption(defaultValue = "") String description
    ) {
        if (transactionHash.getTransactionHash().isBlank()) {
            return CliTransactionHash.ERROR_MESSAGE;
        }
        transactionDescriptionService.set(transactionHash.getTransactionHash(), description);
        return "OK";
    }

    @ShellMethod("Removes a description for the transaction")
    public String removeTransactionDescription(
            @ShellOption(valueProvider = TransactionWithDescriptionCompletionProvider.class)
                    CliTransactionHash transactionHash
    ) {
        if (transactionHash.getTransactionHash().isBlank()) {
            return CliTransactionHash.ERROR_MESSAGE;
        }
        transactionDescriptionService.remove(transactionHash.getTransactionHash());
        return "OK";
    }

    private String formattedHashesSortedByDifferenceForAddress(List<String> hashes, String addressString) {
        Map<String, Transaction> transactionDetails =
                transactionService.getTransactionDetails(Sets.newHashSet(hashes)).parallelStream()
                        .peek(transaction -> priceService.getPrice(transaction.getTime()))
                        .collect(toMap(Transaction::getHash, Functions.identity()));
        String details = hashes.stream()
                .map(hash -> transactionDetails.getOrDefault(hash, Transaction.UNKNOWN))
                .filter(Transaction::isValid)
                .collect(toMap(
                        Functions.identity(),
                        transaction -> transaction.getDifferenceForAddress(addressString).absolute()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(entry -> transactionFormatter.formatSingleLineForAddress(entry.getKey(), addressString))
                .collect(Collectors.joining("\n"));
        if (transactionDetails.containsValue(Transaction.UNKNOWN)) {
            return details + "\n[Details for at least one transaction could not be downloaded]";
        }
        return details;
    }

}

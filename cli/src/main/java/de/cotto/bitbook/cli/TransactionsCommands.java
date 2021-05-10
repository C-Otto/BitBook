package de.cotto.bitbook.cli;

import com.google.common.base.Functions;
import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.transaction.AddressTransactionsService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@ShellComponent
public class TransactionsCommands {

    private final TransactionService transactionService;
    private final AddressTransactionsService addressTransactionsService;
    private final AddressDescriptionService addressDescriptionService;
    private final TransactionDescriptionService transactionDescriptionService;
    private final TransactionFormatter transactionFormatter;
    private final AddressFormatter addressFormatter;
    private final PriceService priceService;
    private final TransactionSorter transactionSorter;

    public TransactionsCommands(
            TransactionService transactionService,
            AddressTransactionsService addressTransactionsService,
            AddressDescriptionService addressDescriptionService,
            TransactionDescriptionService transactionDescriptionService,
            TransactionFormatter transactionFormatter,
            AddressFormatter addressFormatter,
            PriceService priceService,
            TransactionSorter transactionSorter
    ) {
        this.transactionService = transactionService;
        this.addressTransactionsService = addressTransactionsService;
        this.addressDescriptionService = addressDescriptionService;
        this.transactionDescriptionService = transactionDescriptionService;
        this.transactionFormatter = transactionFormatter;
        this.addressFormatter = addressFormatter;
        this.priceService = priceService;
        this.transactionSorter = transactionSorter;
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
        String description = addressDescriptionService.getDescription(addressString);
        Set<String> hashes = addressTransactionsService.getTransactions(addressString).getTransactionHashes();
        String result = """
                Address: %s %s
                Description: %s
                Transaction hashes (%d):
                %s""".formatted(
                addressString,
                addressFormatter.getFormattedOwnershipStatus(addressString),
                description,
                hashes.size(),
                formattedAndSortedHashes(hashes, addressString)
        );
        return StringUtils.stripEnd(result, "\n");
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

    @SuppressWarnings("PMD.LinguisticNaming")
    @ShellMethod("Sets the order used to sort transactions")
    public String setTransactionSortOrder(@ShellOption TransactionSortOrder transactionSortOrder) {
        transactionSorter.setOrder(transactionSortOrder);
        return "OK";
    }

    private String formattedAndSortedHashes(Set<String> hashes, String addressString) {
        Set<Transaction> transactions = transactionService.getTransactionDetails(hashes);
        preloadPrices(transactions);
        String details = transactions.parallelStream()
                .filter(Transaction::isValid)
                .collect(toMap(
                        Functions.identity(),
                        transaction -> transaction.getDifferenceForAddress(addressString).absolute()))
                .entrySet().stream()
                .sorted(transactionSorter.getComparator())
                .map(entry -> transactionFormatter.formatSingleLineForAddress(entry.getKey(), addressString))
                .collect(Collectors.joining("\n"));
        if (transactions.size() != hashes.size() || transactions.contains(Transaction.UNKNOWN)) {
            return details + "\n[Details for at least one transaction could not be downloaded]";
        }
        return details;
    }

    private void preloadPrices(Set<Transaction> transactionDetails) {
        Set<LocalDateTime> transactionTimes = transactionDetails.stream().map(Transaction::getTime).collect(toSet());
        priceService.getPrices(transactionTimes);
    }

}

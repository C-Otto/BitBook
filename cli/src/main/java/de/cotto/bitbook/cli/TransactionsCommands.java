package de.cotto.bitbook.cli;

import com.google.common.base.Functions;
import com.google.common.collect.Sets;
import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.AddressWithDescription;
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

    private static final int TRANSACTION_HASH_LENGTH = 64;
    private final TransactionService transactionService;
    private final AddressTransactionsService addressTransactionsService;
    private final AddressDescriptionService addressDescriptionService;
    private final TransactionDescriptionService transactionDescriptionService;
    private final TransactionFormatter transactionFormatter;
    private final AddressFormatter addressFormatter;

    public TransactionsCommands(
            TransactionService transactionService,
            AddressTransactionsService addressTransactionsService,
            AddressDescriptionService addressDescriptionService,
            TransactionDescriptionService transactionDescriptionService,
            TransactionFormatter transactionFormatter,
            AddressFormatter addressFormatter
    ) {
        this.transactionService = transactionService;
        this.addressTransactionsService = addressTransactionsService;
        this.addressDescriptionService = addressDescriptionService;
        this.transactionDescriptionService = transactionDescriptionService;
        this.transactionFormatter = transactionFormatter;
        this.addressFormatter = addressFormatter;
    }

    @ShellMethod("Get data for a given transaction")
    public String getTransactionDetails(
            @ShellOption(valueProvider = TransactionHashCompletionProvider.class) String transactionHash
    ) {
        String hashJustHexCharacters = transactionHash.replaceAll("[^a-fA-F0-9]", "");
        if (hashJustHexCharacters.length() < TRANSACTION_HASH_LENGTH) {
            return "Expected: 64 hex characters";
        }
        Transaction transaction = transactionService.getTransactionDetails(hashJustHexCharacters);
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
        StringBuilder result = new StringBuilder(36);
        result.append("Address: ").append(addressString)
                .append(' ')
                .append(addressFormatter.getFormattedOwnershipStatus(addressString));
        appendDescription(addressString, result);
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
            @ShellOption(valueProvider = TransactionHashCompletionProvider.class) String transactionHash,
            @ShellOption(defaultValue = "") String description
    ) {
        transactionDescriptionService.set(transactionHash, description);
        return "OK";
    }

    @ShellMethod("Removes a description for the transaction")
    public String removeTransactionDescription(
            @ShellOption(valueProvider = TransactionWithDescriptionCompletionProvider.class) String transactionHash
    ) {
        transactionDescriptionService.remove(transactionHash);
        return "OK";
    }

    private String formattedHashesSortedByDifferenceForAddress(List<String> hashes, String addressString) {
        Map<String, Transaction> transactionDetails =
                transactionService.getTransactionDetails(Sets.newHashSet(hashes)).stream()
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

    private void appendDescription(String address, StringBuilder result) {
        AddressWithDescription addressWithDescription = addressDescriptionService.get(address);
        String description = addressWithDescription.getDescription();
        if (!description.isEmpty()) {
            result.append(" (");
            result.append(description);
            result.append(')');
        }
    }
}

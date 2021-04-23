package de.cotto.bitbook.ownership.cli;

import com.google.common.base.Functions;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.transaction.BalanceService;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import de.cotto.bitbook.cli.AddressCompletionProvider;
import de.cotto.bitbook.cli.AddressWithOwnershipCompletionProvider;
import de.cotto.bitbook.cli.CliAddress;
import de.cotto.bitbook.cli.PriceFormatter;
import de.cotto.bitbook.cli.TransactionFormatter;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

@ShellComponent
public class OwnershipCommands {

    private final AddressOwnershipService addressOwnershipService;
    private final BalanceService balanceService;
    private final PriceService priceService;
    private final PriceFormatter priceFormatter;
    private final TransactionFormatter transactionFormatter;

    public OwnershipCommands(
            AddressOwnershipService addressOwnershipService,
            BalanceService balanceService,
            PriceService priceService,
            PriceFormatter priceFormatter,
            TransactionFormatter transactionFormatter
    ) {
        this.addressOwnershipService = addressOwnershipService;
        this.balanceService = balanceService;
        this.priceService = priceService;
        this.priceFormatter = priceFormatter;
        this.transactionFormatter = transactionFormatter;
    }

    @ShellMethod("Get the total balance over all owned addresses")
    public String getBalance() {
        Coins balance = addressOwnershipService.getBalance();
        Price price = priceService.getCurrentPrice();
        String formattedPrice = priceFormatter.format(balance, price);
        return "%s [%s]".formatted(balance, formattedPrice);
    }

    @ShellMethod("List all owned addresses")
    public String listOwnedAddresses() {
        Price currentPrice = priceService.getCurrentPrice();
        return addressOwnershipService.getOwnedAddressesWithDescription().parallelStream()
                .collect(Collectors.toMap(
                        Functions.identity(),
                        addressWithDescription -> balanceService.getBalance(addressWithDescription.getAddress())
                )).entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(entry -> formatAddressWithPrice(entry.getKey(), entry.getValue(), currentPrice))
                .collect(Collectors.joining("\n"));
    }

    @ShellMethod("Get transactions connected to own addresses where source/target has unknown ownership")
    public String getNeighbourTransactions() {
        Map<Transaction, Coins> map = addressOwnershipService.getNeighbourTransactions();
        preloadPrices(map.keySet());
        Map<Coins, List<Map.Entry<Transaction, Coins>>> byCoins = map.entrySet().stream()
                .collect(groupingBy(Map.Entry::getValue));
        return byCoins.entrySet().stream()
                .map(entry -> new SimpleEntry<>(entry.getKey().absolute(), entry.getValue()))
                .sorted(comparingByKey())
                .filter(coins -> coins.getKey().absolute().isPositive())
                .map(coinsEntry -> appendTransactions(coinsEntry.getValue()))
                .collect(Collectors.joining("\n"));
    }

    @ShellMethod(value = "Mark an address as owened", key = {"mark-address-as-owned", "owned"})
    public String markAddressAsOwned(
            @ShellOption(valueProvider = AddressCompletionProvider.class) CliAddress address,
            @ShellOption(defaultValue = "") String description
    ) {
        String addressString = address.getAddress();
        if (addressString.isEmpty()) {
            return CliAddress.ERROR_MESSAGE;
        }
        addressOwnershipService.setAddressAsOwned(addressString, description);
        return "OK";
    }

    @ShellMethod(value = "Mark an address as foreign (not owned)", key = {"mark-address-as-foreign", "foreign"})
    public String markAddressAsForeign(
            @ShellOption(valueProvider = AddressCompletionProvider.class) CliAddress address,
            @ShellOption(defaultValue = "") String description
    ) {
        String addressString = address.getAddress();
        if (addressString.isEmpty()) {
            return CliAddress.ERROR_MESSAGE;
        }
        addressOwnershipService.setAddressAsForeign(addressString, description);
        return "OK";
    }

    @ShellMethod("Removes information about ownership for the given address")
    public String resetOwnership(
            @ShellOption(valueProvider = AddressWithOwnershipCompletionProvider.class) CliAddress address
    ) {
        String addressString = address.getAddress();
        if (addressString.isEmpty()) {
            return CliAddress.ERROR_MESSAGE;
        }
        addressOwnershipService.resetOwnership(addressString);
        return "OK";
    }

    private String formatAddressWithPrice(
            AddressWithDescription addressWithDescription,
            Coins value,
            Price currentPrice
    ) {
        String formattedPrice = priceFormatter.format(value, currentPrice);
        String coinsWithPrice = "%s [%s]".formatted(value, formattedPrice);
        return addressWithDescription.getFormattedWithInfix(coinsWithPrice);
    }

    private String appendTransactions(List<Map.Entry<Transaction, Coins>> entries) {
        Comparator<Map.Entry<Transaction, Coins>> compareByTransactionHash =
                Comparator.comparing(entry -> entry.getKey().getHash());
        return entries.stream()
                .sorted(compareByTransactionHash)
                .map(transactionWithCoins -> transactionFormatter.formatSingleLineForValue(
                        transactionWithCoins.getKey(),
                        transactionWithCoins.getValue()
                ))
                .collect(Collectors.joining("\n"));
    }

    private void preloadPrices(Set<Transaction> transactions) {
        priceService.getPrices(transactions.stream().map(Transaction::getTime).collect(toSet()));
    }
}

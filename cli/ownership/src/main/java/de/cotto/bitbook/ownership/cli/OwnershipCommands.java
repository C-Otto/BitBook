package de.cotto.bitbook.ownership.cli;

import com.google.common.base.Functions;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.transaction.AddressTransactionsService;
import de.cotto.bitbook.backend.transaction.BalanceService;
import de.cotto.bitbook.cli.AddressCompletionProvider;
import de.cotto.bitbook.cli.AddressWithOwnershipCompletionProvider;
import de.cotto.bitbook.cli.CliAddress;
import de.cotto.bitbook.cli.PriceFormatter;
import de.cotto.bitbook.cli.SelectedChain;
import de.cotto.bitbook.cli.TransactionFormatter;
import de.cotto.bitbook.cli.TransactionSorter;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toSet;

@ShellComponent
public class OwnershipCommands {

    private final AddressOwnershipService addressOwnershipService;
    private final BalanceService balanceService;
    private final PriceService priceService;
    private final PriceFormatter priceFormatter;
    private final TransactionFormatter transactionFormatter;
    private final AddressTransactionsService addressTransactionsService;
    private final TransactionSorter transactionSorter;
    private final SelectedChain selectedChain;

    public OwnershipCommands(
            AddressOwnershipService addressOwnershipService,
            BalanceService balanceService,
            PriceService priceService,
            PriceFormatter priceFormatter,
            TransactionFormatter transactionFormatter,
            AddressTransactionsService addressTransactionsService,
            TransactionSorter transactionSorter,
            SelectedChain selectedChain
    ) {
        this.addressOwnershipService = addressOwnershipService;
        this.balanceService = balanceService;
        this.priceService = priceService;
        this.priceFormatter = priceFormatter;
        this.transactionFormatter = transactionFormatter;
        this.addressTransactionsService = addressTransactionsService;
        this.transactionSorter = transactionSorter;
        this.selectedChain = selectedChain;
    }

    @ShellMethod("Get the total balance over all owned addresses")
    public String getBalance() {
        Coins balance = addressOwnershipService.getBalance(selectedChain.getChain());
        Price price = priceService.getCurrentPrice(selectedChain.getChain());
        String formattedPrice = priceFormatter.format(balance, price);
        return "%s [%s]".formatted(balance, formattedPrice);
    }

    @ShellMethod("Get all owned addresses")
    public String getOwnedAddresses() {
        Chain chain = selectedChain.getChain();
        Price currentPrice = priceService.getCurrentPrice(chain);
        Set<AddressWithDescription> ownedAddressesWithDescription =
                addressOwnershipService.getOwnedAddressesWithDescription();
        preloadAddressTransactions(ownedAddressesWithDescription, chain);
        return ownedAddressesWithDescription.parallelStream()
                .filter(this::hasAtLeastOneTransaction)
                .collect(Collectors.toMap(
                        Functions.identity(),
                        addressWithDescription -> balanceService.getBalance(addressWithDescription.getAddress(), chain)
                )).entrySet().stream()
                .sorted(comparingByValue())
                .map(entry -> formatAddressWithPrice(entry.getKey(), entry.getValue(), currentPrice))
                .collect(Collectors.joining("\n"));
    }

    @ShellMethod("Get all transactions that touch at least one owned address")
    public String getMyTransactions() {
        Map<Transaction, Coins> transactionsWithCoins =
                addressOwnershipService.getMyTransactionsWithCoins(selectedChain.getChain());
        preloadPrices(transactionsWithCoins.keySet());
        return transactionsWithCoins.entrySet().stream()
                .sorted(transactionSorter.getComparator())
                .map(entry -> transactionFormatter.formatSingleLineForValue(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"));
    }

    @ShellMethod("Get transactions connected to own addresses where source/target has unknown ownership")
    public String getNeighbourTransactions() {
        Map<Transaction, Coins> transactionsWithCoins =
                addressOwnershipService.getNeighbourTransactions(selectedChain.getChain());
        preloadPrices(transactionsWithCoins.keySet());
        return transactionsWithCoins.entrySet().stream()
                .filter(entry -> entry.getValue().absolute().isPositive())
                .sorted(transactionSorter.getComparator())
                .map(entry -> transactionFormatter.formatSingleLineForValue(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"));
    }

    @ShellMethod(value = "Mark an address as owned", key = {"mark-address-as-owned", "owned"})
    public String markAddressAsOwned(
            @ShellOption(valueProvider = AddressCompletionProvider.class) CliAddress address,
            @ShellOption(defaultValue = "") String description
    ) {
        Address addressModel = address.getAddress();
        if (addressModel.isInvalid()) {
            return CliAddress.ERROR_MESSAGE;
        }
        addressOwnershipService.setAddressAsOwned(addressModel, selectedChain.getChain(), description);
        return "OK";
    }

    @ShellMethod(value = "Mark an address as foreign (not owned)", key = {"mark-address-as-foreign", "foreign"})
    public String markAddressAsForeign(
            @ShellOption(valueProvider = AddressCompletionProvider.class) CliAddress address,
            @ShellOption(defaultValue = "") String description
    ) {
        Address addressModel = address.getAddress();
        if (addressModel.isInvalid()) {
            return CliAddress.ERROR_MESSAGE;
        }
        addressOwnershipService.setAddressAsForeign(addressModel, description);
        return "OK";
    }

    @ShellMethod("Removes information about ownership for the given address")
    public String resetOwnership(
            @ShellOption(valueProvider = AddressWithOwnershipCompletionProvider.class) CliAddress address
    ) {
        Address addressModel = address.getAddress();
        if (addressModel.isInvalid()) {
            return CliAddress.ERROR_MESSAGE;
        }
        addressOwnershipService.resetOwnership(addressModel);
        return "OK";
    }

    private void preloadAddressTransactions(Set<AddressWithDescription> addressesWithDescription, Chain chain) {
        Set<Address> ownedAddresses = addressesWithDescription.stream()
                .map(AddressWithDescription::getAddress)
                .collect(toSet());
        addressTransactionsService.getTransactionsForAddresses(ownedAddresses, chain);
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

    private void preloadPrices(Set<Transaction> transactions) {
        priceService.getPrices(transactions.stream()
                .map(Transaction::getTime).collect(toSet()), selectedChain.getChain());
    }

    private boolean hasAtLeastOneTransaction(AddressWithDescription addressWithDescription) {
        Address address = addressWithDescription.getAddress();
        Chain chain = selectedChain.getChain();
        AddressTransactions transactions = addressTransactionsService.getTransactions(address, chain);
        return !transactions.transactionHashes().isEmpty();
    }
}

package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.transaction.BalanceService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class AddressCommands {

    private final BalanceService balanceService;
    private final PriceService priceService;
    private final PriceFormatter priceFormatter;
    private final AddressDescriptionService addressDescriptionService;
    private final SelectedChain selectedChain;

    public AddressCommands(
            BalanceService balanceService,
            PriceService priceService,
            PriceFormatter priceFormatter,
            AddressDescriptionService addressDescriptionService,
            SelectedChain selectedChain
    ) {
        this.balanceService = balanceService;
        this.priceService = priceService;
        this.priceFormatter = priceFormatter;
        this.addressDescriptionService = addressDescriptionService;
        this.selectedChain = selectedChain;
    }

    @ShellMethod("Get balance for address")
    public String getBalanceForAddress(
            @ShellOption(valueProvider = AddressCompletionProvider.class) CliAddress address
    ) {
        Address addressModel = address.getAddress();
        if (addressModel.isInvalid()) {
            return CliAddress.ERROR_MESSAGE;
        }
        Coins balance = balanceService.getBalance(addressModel);
        Price price = priceService.getCurrentPrice(selectedChain.getChain());
        return "%s [%s]".formatted(balance, priceFormatter.format(balance, price));
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    @ShellMethod("Sets a description for the address")
    public String setAddressDescription(
            @ShellOption(valueProvider = AddressCompletionProvider.class) CliAddress address,
            @ShellOption(defaultValue = "") String description
    ) {
        Address addressModel = address.getAddress();
        if (addressModel.isInvalid()) {
            return CliAddress.ERROR_MESSAGE;
        }
        addressDescriptionService.set(addressModel, description);
        return "OK";
    }

    @ShellMethod("Removes a description for the address")
    public String removeAddressDescription(
            @ShellOption(valueProvider = AddressWithDescriptionCompletionProvider.class) CliAddress address
    ) {
        Address addressModel = address.getAddress();
        if (addressModel.isInvalid()) {
            return CliAddress.ERROR_MESSAGE;
        }
        addressDescriptionService.remove(addressModel);
        return "OK";
    }
}

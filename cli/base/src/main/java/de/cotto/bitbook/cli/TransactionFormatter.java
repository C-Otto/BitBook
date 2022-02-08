package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.InputOutput;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TransactionFormatter {
    private final AddressDescriptionService addressDescriptionService;
    private final TransactionDescriptionService transactionDescriptionService;
    private final AddressFormatter addressFormatter;
    private final PriceService priceService;
    private final PriceFormatter priceFormatter;
    private final AddressOwnershipService addressOwnershipService;
    private final SelectedChain selectedChain;

    public TransactionFormatter(
            AddressDescriptionService addressDescriptionService,
            TransactionDescriptionService transactionDescriptionService,
            AddressFormatter addressFormatter,
            PriceService priceService,
            PriceFormatter priceFormatter,
            AddressOwnershipService addressOwnershipService,
            SelectedChain selectedChain
    ) {
        this.addressDescriptionService = addressDescriptionService;
        this.transactionDescriptionService = transactionDescriptionService;
        this.addressFormatter = addressFormatter;
        this.priceService = priceService;
        this.priceFormatter = priceFormatter;
        this.addressOwnershipService = addressOwnershipService;
        this.selectedChain = selectedChain;
    }

    public String format(Transaction transaction) {
        String formattedTime = transaction.getTime().format(DateTimeFormatter.ISO_DATE_TIME);
        Price price = priceService.getPrice(transaction.getTime(), selectedChain.getChain());
        String fees = formatWithPrice(transaction.getFees(), price);
        Coins differenceToOwnedAddresses =
                transaction.getDifferenceForAddresses(addressOwnershipService.getOwnedAddresses());
        String contribution = formatWithPrice(differenceToOwnedAddresses, price);
        String transactionDescription = transactionDescriptionService.getDescription(transaction.getHash());
        return """
               Transaction:\t%s
               Description:\t%s
               Block:\t\t%d (%s)
               Fees:\t\t%s
               Contribution:\t%s
               Inputs:%n%s
               Outputs:%n%s
               """.formatted(transaction.getHash(),
                transactionDescription,
                transaction.getBlockHeight(),
                formattedTime,
                fees,
                contribution,
                formatInputsOutputs(transaction.getInputs(), price),
                formatInputsOutputs(transaction.getOutputs(), price)
        );
    }

    private String formatInputsOutputs(List<? extends InputOutput> inputsOutputs, Price price) {
        StringBuilder result = new StringBuilder();
        inputsOutputs.stream()
                .collect(Collectors.toMap(InputOutput::getAddress, InputOutput::getValue, Coins::add))
                .entrySet().stream()
                .sorted(Map.Entry.<Address, Coins>comparingByValue().reversed())
                .forEach(entry -> {
                    Coins coins = entry.getValue();
                    Address address = entry.getKey();
                    String infix = getFormattedOwnershipStatus(address) + " " + formatWithPrice(coins, price);
                    result.append(addressDescriptionService.get(address).getFormattedWithInfix(infix));
                    result.append('\n');
                });
        return StringUtils.stripEnd(result.toString(), "\n");
    }

    public String formatSingleLineForAddress(Transaction transaction, Address address) {
        Coins differenceForAddress = transaction.getDifferenceForAddress(address);
        return formatSingleLineForValue(transaction, differenceForAddress);
    }

    public String formatSingleLineForValue(Transaction transaction, Coins value) {
        Price price = priceService.getPrice(transaction.getTime(), selectedChain.getChain());
        String description = transactionDescriptionService.get(transaction.getHash()).getFormattedDescription();
        String descriptionSuffix;
        if (description.isBlank()) {
            descriptionSuffix = "";
        } else {
            descriptionSuffix = " " + description;
        }
        return "%s: %s (block %d, %s)%s".formatted(
                transaction.getHash(),
                formatWithPrice(value, price),
                transaction.getBlockHeight(),
                transaction.getTime().format(DateTimeFormatter.ISO_DATE_TIME),
                descriptionSuffix
        );
    }

    public String getFormattedOwnershipStatus(Address address) {
        return addressFormatter.getFormattedOwnershipStatus(address);
    }

    private String formatWithPrice(Coins coins, Price price) {
        return coins + " [" + priceFormatter.format(coins, price) + "]";
    }
}

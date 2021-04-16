package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.InputOutput;
import de.cotto.bitbook.backend.transaction.model.Transaction;
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

    public TransactionFormatter(
            AddressDescriptionService addressDescriptionService,
            TransactionDescriptionService transactionDescriptionService,
            AddressFormatter addressFormatter,
            PriceService priceService,
            PriceFormatter priceFormatter
    ) {
        this.addressDescriptionService = addressDescriptionService;
        this.transactionDescriptionService = transactionDescriptionService;
        this.addressFormatter = addressFormatter;
        this.priceService = priceService;
        this.priceFormatter = priceFormatter;
    }

    public String format(Transaction transaction) {
        String formattedTime = transaction.getTime().format(DateTimeFormatter.ISO_DATE_TIME);
        Price price = priceService.getPrice(transaction.getTime());
        String fees = formatWithPrice(transaction.getFees(), price);
        String transactionDescription = transactionDescriptionService.getDescription(transaction.getHash());
        return """
               Transaction:\t%s
               Description:\t%s
               Block:\t\t%d (%s)
               Fees:\t\t%s
               Inputs:%n%s
               Outputs:%n%s
               """.formatted(transaction.getHash(),
                transactionDescription,
                transaction.getBlockHeight(),
                formattedTime,
                fees,
                formatInputsOutputs(transaction.getInputs(), price),
                formatInputsOutputs(transaction.getOutputs(), price)
        );
    }

    private String formatInputsOutputs(List<? extends InputOutput> inputsOutputs, Price price) {
        StringBuilder result = new StringBuilder();
        inputsOutputs.stream()
                .collect(Collectors.toMap(InputOutput::getAddress, InputOutput::getValue, Coins::add))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Coins>comparingByValue().reversed())
                .forEach(entry -> {
                    Coins coins = entry.getValue();
                    String address = entry.getKey();
                    String infix = getFormattedOwnershipStatus(address) + " " + formatWithPrice(coins, price);
                    result.append(addressDescriptionService.get(address).getFormattedWithInfix(infix));
                    result.append('\n');
                });
        return StringUtils.stripEnd(result.toString(), "\n");
    }

    public String formatSingleLineForAddress(Transaction transaction, String address) {
        Coins differenceForAddress = transaction.getDifferenceForAddress(address);
        return formatSingleLineForValue(transaction, differenceForAddress);
    }

    public String formatSingleLineForValue(Transaction transaction, Coins value) {
        Price price = priceService.getPrice(transaction.getTime());
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

    public String getFormattedOwnershipStatus(String address) {
        return addressFormatter.getFormattedOwnershipStatus(address);
    }

    private String formatWithPrice(Coins coins, Price price) {
        return coins + " [" + priceFormatter.format(coins, price) + "]";
    }
}

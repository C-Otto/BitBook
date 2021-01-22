package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
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
    private final AddressFormatter addressFormatter;
    private final PriceService priceService;
    private final PriceFormatter priceFormatter;

    public TransactionFormatter(
            AddressDescriptionService addressDescriptionService,
            AddressFormatter addressFormatter,
            PriceService priceService,
            PriceFormatter priceFormatter
    ) {
        this.addressDescriptionService = addressDescriptionService;
        this.addressFormatter = addressFormatter;
        this.priceService = priceService;
        this.priceFormatter = priceFormatter;
    }

    public String format(Transaction transaction) {
        String formattedTime = transaction.getTime().format(DateTimeFormatter.ISO_DATE_TIME);
        Price price = priceService.getPrice(transaction.getTime());
        String fees = formatWithPrice(transaction.getFees(), price);
        return """
               Transaction:\t%s
               Height:\t\t%d (%s)
               Fees:\t\t%s
               Inputs:%n%s
               Outputs:%n%s
               """.formatted(transaction.getHash(),
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
        Price price = priceService.getPrice(transaction.getTime());
        return "%s: %s (block height %d, %s)".formatted(
                transaction.getHash(),
                formatWithPrice(transaction.getDifferenceForAddress(address), price),
                transaction.getBlockHeight(),
                transaction.getTime().format(DateTimeFormatter.ISO_DATE_TIME)
        );
    }

    public String getFormattedOwnershipStatus(String address) {
        return addressFormatter.getFormattedOwnershipStatus(address);
    }

    private String formatWithPrice(Coins coins, Price price) {
        return coins + " [" + priceFormatter.format(coins, price) + "]";
    }
}

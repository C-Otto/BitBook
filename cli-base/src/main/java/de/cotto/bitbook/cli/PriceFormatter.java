package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.price.model.Price;
import de.cotto.bitbook.backend.transaction.model.Coins;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Component
public class PriceFormatter {
    private static final int MINIMUM_LENGTH = 13;

    private final DecimalFormat decimalFormat;

    public PriceFormatter() {
        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance(new Locale("en", "US"));
        decimalFormat = new DecimalFormat("#,##0.00", decimalFormatSymbols);
    }

    public String format(Coins coins, Price price) {
        if (price.equals(Price.UNKNOWN)) {
            return " Price unknown";
        }
        BigDecimal valueAtPrice = BigDecimal.valueOf(coins.getSatoshis())
                .multiply(price.getAsBigDecimal())
                .divide(BigDecimal.valueOf(Coins.SATOSHIS_IN_COIN), RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
        String formatted = decimalFormat.format(valueAtPrice);
        return StringUtils.leftPad(formatted, MINIMUM_LENGTH) + "€";
    }
}

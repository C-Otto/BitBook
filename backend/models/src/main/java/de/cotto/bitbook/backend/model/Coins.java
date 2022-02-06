package de.cotto.bitbook.backend.model;

import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

import java.math.BigDecimal;
import java.util.Locale;

public class Coins implements Comparable<Coins> {
    private static final int COIN_SCALE = 8;
    public static final BigDecimal SATOSHIS_IN_COIN = BigDecimal.valueOf(1, -COIN_SCALE);
    public static final Coins NONE = Coins.ofSatoshis(0);

    private final long satoshis;

    protected Coins(long satoshis) {
        this.satoshis = satoshis;
    }

    public static Coins ofSatoshis(long satoshis) {
        return new Coins(satoshis);
    }

    public long getSatoshis() {
        return satoshis;
    }

    public Coins add(Coins summand) {
        return Coins.ofSatoshis(satoshis + summand.satoshis);
    }

    public Coins subtract(Coins subtrahend) {
        return Coins.ofSatoshis(satoshis - subtrahend.satoshis);
    }

    public Coins absolute() {
        return Coins.ofSatoshis(Math.abs(satoshis));
    }

    @Override
    public int compareTo(Coins other) {
        return Long.compare(satoshis, other.satoshis);
    }

    public boolean isPositive() {
        return compareTo(NONE) > 0;
    }

    public boolean isNegative() {
        return compareTo(NONE) < 0;
    }

    public boolean isNonPositive() {
        return !isPositive();
    }

    public boolean isNonNegative() {
        return !isNegative();
    }

    @Override
    public String toString() {
        String formatted = getWithoutColor();
        String wholeCoin = formatted.substring(0, formatted.length() - 9);
        char decimalPoint = formatted.charAt(formatted.length() - 9);
        String milliCoin = formatted.substring(formatted.length() - 8, formatted.length() - 5);
        String satoshi = formatted.substring(formatted.length() - 5);
        return AnsiOutput.toString(
                AnsiColor.BRIGHT_GREEN, wholeCoin, AnsiColor.DEFAULT,
                decimalPoint,
                AnsiColor.RED, milliCoin, AnsiColor.DEFAULT,
                AnsiColor.YELLOW, satoshi, AnsiColor.DEFAULT
        );
    }

    private String getWithoutColor() {
        double coins = BigDecimal.valueOf(satoshis, COIN_SCALE).doubleValue();
        String result = String.format(Locale.ENGLISH, "%13.8f", coins);
        StringBuilder suffix = new StringBuilder();
        while (result.endsWith("0")) {
            result = withoutLastCharacter(result);
            suffix.append(' ');
        }
        if (result.endsWith(".")) {
            result = withoutLastCharacter(result);
            suffix.append(' ');
        }
        return result + suffix;
    }

    private String withoutLastCharacter(String string) {
        return string.substring(0, string.length() - 1);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Coins coins = (Coins) other;

        return satoshis == coins.satoshis;
    }

    @Override
    public int hashCode() {
        return (int) (satoshis ^ (satoshis >>> 32));
    }
}

package de.cotto.bitbook.cli;

public enum TransactionSortOrder {
    BY_HASH,

    BY_COINS_THEN_HASH,
    BY_COINS_THEN_DATE_THEN_HASH,

    BY_COINS_ABSOLUTE_THEN_HASH,
    BY_COINS_ABSOLUTE_THEN_DATE_THEN_HASH,

    BY_DATE_THEN_COINS_THEN_HASH,
    BY_DATE_THEN_COINS_ABSOLUTE_THEN_HASH,

    BY_DATE_THEN_HASH
}

package de.cotto.bitbook.backend.transaction.bitaps;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.deserialization.TestObjectMapper;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.backend.transaction.bitaps.BitapsAddressTransactionDtoFixtures.BITAPS_ADDRESS_TRANSACTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BitapsAddressTransactionsDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void toModel() {
        assertThat(BITAPS_ADDRESS_TRANSACTIONS.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization() throws Exception {
        String json = """
                {
                  "data": {
                      "pages": 1,
                      "list": [
                         {
                            "txId": "%s"
                         },
                         {
                            "txId": "%s"
                         }
                      ]
                  }
                }""".formatted(TRANSACTION_HASH, TRANSACTION_HASH_2);
        BitapsAddressTransactionsDto bitapsAddressTransactionsDto =
                objectMapper.readValue(json, BitapsAddressTransactionsDto.class);
        assertThat(bitapsAddressTransactionsDto.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization_paginated() {
        String json = """
                {
                  "data": {
                      "pages": 2,
                      "list": [
                         {
                            "txId": "%s"
                         },
                         {
                            "txId": "%s"
                         }
                      ]
                  }
                }""".formatted(TRANSACTION_HASH, TRANSACTION_HASH_2);
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                objectMapper.readValue(json, BitapsAddressTransactionsDto.class)
        );
    }
}
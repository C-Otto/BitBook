package de.cotto.bitbook.backend.transaction.btccom;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.deserialization.TestObjectMapper;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.backend.transaction.btccom.BtcComAddressTransactionsFixtures.BTCCOM_ADDRESS_DETAILS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BtcComAddressTransactionsDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void toModel() {
        assertThat(BTCCOM_ADDRESS_DETAILS.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization() throws Exception {
        String json = """
                {
                  "data": {
                    "total_count": 2,
                    "list": [
                      {
                        "hash": "%s"
                      },
                      {
                        "hash": "%s"
                      }
                    ]
                  }
                }""".formatted(TRANSACTION_HASH, TRANSACTION_HASH_2);
        BtcComAddressTransactionsDto btccomTransactionDto =
                objectMapper.readValue(json, BtcComAddressTransactionsDto.class);
        assertThat(btccomTransactionDto.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization_number_does_not_match() {
        String json = """
                {
                  "data": {
                    "total_count": 3,
                    "list": [
                      {
                        "hash": "%s"
                      },
                      {
                        "hash": "%s"
                      }
                    ]
                  }
                }""".formatted(TRANSACTION_HASH, TRANSACTION_HASH_2);
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
            objectMapper.readValue(json, BtcComAddressTransactionsDto.class)
        );
    }
}
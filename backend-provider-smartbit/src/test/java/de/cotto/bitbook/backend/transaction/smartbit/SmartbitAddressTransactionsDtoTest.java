package de.cotto.bitbook.backend.transaction.smartbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.deserialization.TestObjectMapper;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.backend.transaction.smartbit.SmartbitAddressTransactionDtoFixtures.SMARTBIT_ADDRESS_TRANSACTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SmartbitAddressTransactionsDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void toModel() {
        assertThat(SMARTBIT_ADDRESS_TRANSACTIONS.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization() throws Exception {
        String json = """
                {
                  "address": {
                      "address": "%s",
                      "confirmed": {
                        "transaction_count": 2
                      },
                      "transactions": [
                         {
                            "txid": "%s"
                         },
                         {
                            "txid": "%s"
                         }
                      ]
                  }
                }""".formatted(ADDRESS, TRANSACTION_HASH, TRANSACTION_HASH_2);
        SmartbitAddressTransactionsDto smartbitAddressTransactionsDto =
                objectMapper.readValue(json, SmartbitAddressTransactionsDto.class);
        assertThat(smartbitAddressTransactionsDto.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization_wrong_address() {
        String json = """
                {
                  "address": {
                      "address": "%s",
                      "confirmed": {
                        "transaction_count": 2
                      },
                      "transactions": [
                         {
                            "txid": "%s"
                         },
                         {
                            "txid": "%s"
                         }
                      ]
                  }
                }""".formatted("xxx", TRANSACTION_HASH, TRANSACTION_HASH_2);
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                objectMapper.readValue(json, SmartbitAddressTransactionsDto.class)
                        .toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS)
        );
    }

    @Test
    void deserialization_number_does_not_match() {
        String json = """
                {
                  "address": {
                      "address": "%s",
                      "confirmed": {
                        "transaction_count": 3
                      },
                      "transactions": [
                         {
                            "txid": "%s"
                         },
                         {
                            "txid": "%s"
                         }
                      ]
                  }
                }""".formatted(ADDRESS, TRANSACTION_HASH, TRANSACTION_HASH_2);
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                objectMapper.readValue(json, SmartbitAddressTransactionsDto.class)
        );
    }
}
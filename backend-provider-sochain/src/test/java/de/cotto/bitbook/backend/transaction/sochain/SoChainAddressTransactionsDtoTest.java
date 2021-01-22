package de.cotto.bitbook.backend.transaction.sochain;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.deserialization.TestObjectMapper;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.backend.transaction.sochain.SoChainAddressTransactionsFixtures.SOCHAIN_ADDRESS_DETAILS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SoChainAddressTransactionsDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void toModel() {
        assertThat(SOCHAIN_ADDRESS_DETAILS.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization() throws Exception {
        String json = """
                {
                  "data": {
                      "address": "%s",
                      "total_txs": 2,
                      "txs": [
                         {
                            "txid": "%s"
                         },
                         {
                            "txid": "%s"
                         }
                      ]
                  }
                }""".formatted(ADDRESS, TRANSACTION_HASH, TRANSACTION_HASH_2);
        SoChainAddressTransactionsDto soChainTransactionDto =
                objectMapper.readValue(json, SoChainAddressTransactionsDto.class);
        assertThat(soChainTransactionDto.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization_wrong_address() {
        String json = """
                {
                  "data": {
                      "address": "%s",
                      "total_txs": 2,
                      "txs": [
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
                objectMapper.readValue(json, SoChainAddressTransactionsDto.class)
                        .toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS)
        );
    }

    @Test
    void deserialization_number_does_not_match() {
        String json = """
                {
                  "data": {
                      "address": "%s",
                      "total_txs": 3,
                      "txs": [
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
            objectMapper.readValue(json, SoChainAddressTransactionsDto.class)
        );
    }
}
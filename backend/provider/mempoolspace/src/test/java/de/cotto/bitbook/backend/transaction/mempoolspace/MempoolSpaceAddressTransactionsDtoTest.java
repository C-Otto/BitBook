package de.cotto.bitbook.backend.transaction.mempoolspace;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.deserialization.TestObjectMapper;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.backend.transaction.mempoolspace.MempoolSpaceAddressTransactionsFixtures.MEMPOOLSPACE_ADDRESS_DETAILS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MempoolSpaceAddressTransactionsDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void toModel() {
        assertThat(MEMPOOLSPACE_ADDRESS_DETAILS.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization() throws Exception {
        String json = """
                [
                  {
                    "txid": "%s"
                  },
                  {
                    "txid": "%s"
                  }
                ]""".formatted(TRANSACTION_HASH, TRANSACTION_HASH_2);
        MempoolSpaceAddressTransactionsDto mempoolspaceTransactionDto =
                objectMapper.readValue(json, MempoolSpaceAddressTransactionsDto.class);
        assertThat(mempoolspaceTransactionDto.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization_maximum_number_of_transactions() {
        // I don't know how to see if the address has 25 transactions or more
        String json = """
                [
                  { "txid": "1" },
                  { "txid": "2" },
                  { "txid": "3" },
                  { "txid": "4" },
                  { "txid": "5" },
                  { "txid": "6" },
                  { "txid": "7" },
                  { "txid": "8" },
                  { "txid": "9" },
                  { "txid": "10" },
                  { "txid": "11" },
                  { "txid": "12" },
                  { "txid": "13" },
                  { "txid": "14" },
                  { "txid": "15" },
                  { "txid": "16" },
                  { "txid": "17" },
                  { "txid": "18" },
                  { "txid": "19" },
                  { "txid": "20" },
                  { "txid": "21" },
                  { "txid": "22" },
                  { "txid": "23" },
                  { "txid": "24" },
                  { "txid": "25" }
                ]""";
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
            objectMapper.readValue(json, MempoolSpaceAddressTransactionsDto.class)
        );
    }
}
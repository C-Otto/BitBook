package de.cotto.bitbook.backend.transaction.blockstream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.deserialization.TestObjectMapper;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_2;
import static de.cotto.bitbook.backend.transaction.blockstream.BlockstreamAddressTransactionsFixtures.BLOCKSTREAM_ADDRESS_DETAILS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BlockstreamAddressTransactionsDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void toModel() {
        assertThat(BLOCKSTREAM_ADDRESS_DETAILS.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS, BTC))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization() throws Exception {
        String json = """
                [
                  {
                    "txid": "%s",
                    "status": {
                        "confirmed": true
                    }
                  },
                  {
                    "txid": "%s",
                    "status": {
                        "confirmed": true
                    }
                  }
                ]""".formatted(TRANSACTION_HASH, TRANSACTION_HASH_2);
        BlockstreamAddressTransactionsDto addressTransactionsDto =
                objectMapper.readValue(json, BlockstreamAddressTransactionsDto.class);
        assertThat(addressTransactionsDto.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS, BTC))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization_ignores_unconfirmed_transaction() throws Exception {
        String json = """
                [
                  {
                    "txid": "unconfirmed",
                    "status": {
                        "confirmed": false
                    }
                  },
                  {
                    "txid": "%s",
                    "status": {
                        "confirmed": true
                    }
                  },
                  {
                   "txid": "%s",
                   "status": {
                        "confirmed": true
                    }
                  }
                ]""".formatted(TRANSACTION_HASH, TRANSACTION_HASH_2);
        BlockstreamAddressTransactionsDto addressTransactionsDto =
                objectMapper.readValue(json, BlockstreamAddressTransactionsDto.class);
        assertThat(addressTransactionsDto.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS, BTC))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization_maximum_number_of_transactions() {
        // I don't know how to see if the address has 25 transactions or more
        String json = """
                [
                  { "txid": "1" , "status": {"confirmed": true}},
                  { "txid": "2" , "status": {"confirmed": true}},
                  { "txid": "3" , "status": {"confirmed": true}},
                  { "txid": "4" , "status": {"confirmed": true}},
                  { "txid": "5" , "status": {"confirmed": true}},
                  { "txid": "6" , "status": {"confirmed": true}},
                  { "txid": "7" , "status": {"confirmed": true}},
                  { "txid": "8" , "status": {"confirmed": true}},
                  { "txid": "9" , "status": {"confirmed": true}},
                  { "txid": "10" , "status": {"confirmed": true}},
                  { "txid": "11" , "status": {"confirmed": true}},
                  { "txid": "12" , "status": {"confirmed": true}},
                  { "txid": "13" , "status": {"confirmed": true}},
                  { "txid": "14" , "status": {"confirmed": true}},
                  { "txid": "15" , "status": {"confirmed": true}},
                  { "txid": "16" , "status": {"confirmed": true}},
                  { "txid": "17" , "status": {"confirmed": true}},
                  { "txid": "18" , "status": {"confirmed": true}},
                  { "txid": "19" , "status": {"confirmed": true}},
                  { "txid": "20" , "status": {"confirmed": true}},
                  { "txid": "21" , "status": {"confirmed": true}},
                  { "txid": "22" , "status": {"confirmed": true}},
                  { "txid": "23" , "status": {"confirmed": true}},
                  { "txid": "24" , "status": {"confirmed": true}},
                  { "txid": "25" , "status": {"confirmed": true}}
                ]""";
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
            objectMapper.readValue(json, BlockstreamAddressTransactionsDto.class)
        );
    }

    @Test
    void deserialization_only_counts_confirmed_transactions() throws JsonProcessingException {
        String json = """
                [
                  { "txid": "1" , "status": {"confirmed": false}},
                  { "txid": "2" , "status": {"confirmed": true}},
                  { "txid": "3" , "status": {"confirmed": true}},
                  { "txid": "4" , "status": {"confirmed": true}},
                  { "txid": "5" , "status": {"confirmed": true}},
                  { "txid": "6" , "status": {"confirmed": true}},
                  { "txid": "7" , "status": {"confirmed": true}},
                  { "txid": "8" , "status": {"confirmed": true}},
                  { "txid": "9" , "status": {"confirmed": true}},
                  { "txid": "10" , "status": {"confirmed": true}},
                  { "txid": "11" , "status": {"confirmed": true}},
                  { "txid": "12" , "status": {"confirmed": true}},
                  { "txid": "13" , "status": {"confirmed": true}},
                  { "txid": "14" , "status": {"confirmed": true}},
                  { "txid": "15" , "status": {"confirmed": true}},
                  { "txid": "16" , "status": {"confirmed": true}},
                  { "txid": "17" , "status": {"confirmed": true}},
                  { "txid": "18" , "status": {"confirmed": true}},
                  { "txid": "19" , "status": {"confirmed": true}},
                  { "txid": "20" , "status": {"confirmed": true}},
                  { "txid": "21" , "status": {"confirmed": true}},
                  { "txid": "22" , "status": {"confirmed": true}},
                  { "txid": "23" , "status": {"confirmed": true}},
                  { "txid": "24" , "status": {"confirmed": true}},
                  { "txid": "25" , "status": {"confirmed": true}}
                ]""";
        BlockstreamAddressTransactionsDto addressTransactionsDto =
                objectMapper.readValue(json, BlockstreamAddressTransactionsDto.class);
        assertThat(addressTransactionsDto.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS, BTC).transactionHashes())
                .hasSize(24);
    }
}
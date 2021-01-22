package de.cotto.bitbook.backend.transaction.blockcypher;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.deserialization.TestObjectMapper;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.transaction.blockcypher.BlockcypherAddressTransactionsFixtures.ADDRESS_DETAILS_INCOMPLETE;
import static de.cotto.bitbook.backend.transaction.blockcypher.BlockcypherAddressTransactionsFixtures.ADDRESS_DETAILS_SECOND_PART;
import static de.cotto.bitbook.backend.transaction.blockcypher.BlockcypherAddressTransactionsFixtures.BLOCKCYPHER_ADDRESS_DETAILS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BlockcypherAddressTransactionsDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void toModel() {
        assertThat(BLOCKCYPHER_ADDRESS_DETAILS.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization() throws Exception {
        String json = """
                {
                   "address": "%s",
                   "txrefs": [
                     {
                       "tx_hash": "%s",
                       "value": 1,
                       "block_height": %d
                     },
                     {
                       "tx_hash": "%s",
                       "value": 1,
                       "block_height": %d
                     }
                   ]
                }""".formatted(ADDRESS, TRANSACTION_HASH, 123, TRANSACTION_HASH_2, 300_000);
        BlockcypherAddressTransactionsDto blockcypherTransactionDto =
                objectMapper.readValue(json, BlockcypherAddressTransactionsDto.class);
        assertThat(blockcypherTransactionDto.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization_wrong_address() {
        String json = """
                {
                   "address": "%s",
                   "txrefs": [
                     {
                       "tx_hash": "%s",
                       "value": 1,
                       "block_height": %d
                     },
                     {
                       "tx_hash": "%s",
                       "value": 1,
                       "block_height": %d
                     }
                   ]
                }""".formatted("xxx", TRANSACTION_HASH, 123, TRANSACTION_HASH_2, 300_000);
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                objectMapper.readValue(json, BlockcypherAddressTransactionsDto.class)
                        .toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS)
        );
    }

    @Test
    void deserialization_large_transaction() throws Exception {
        String json = """
                {
                   "address": "%s",
                   "txrefs": [
                     {
                       "tx_hash": "%s",
                       "value": 2147483648,
                       "block_height": %d
                     }
                   ]
                }""".formatted(ADDRESS, TRANSACTION_HASH, 123);
        BlockcypherAddressTransactionsDto blockcypherTransactionDto =
                objectMapper.readValue(json, BlockcypherAddressTransactionsDto.class);
        assertThat(blockcypherTransactionDto.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS)
                .getTransactionHashes()).hasSize(1);
    }

    @Test
    void deserialization_ignores_outputs_with_zero_value() throws Exception {
        int zeroValue = 0;
        int value = 18_263;
        String json = """
                {
                   "address": "1DEP8i3QJCsomS4BSMY2RpU1upv62aGvhD",
                   "txrefs": [
                     {
                       "tx_hash": "%s",
                       "block_height": 669571,
                       "value": %d
                     },
                     {
                       "tx_hash": "%s",
                       "block_height": 300000,
                       "value": %d
                     }
                   ]
                }""".formatted(TRANSACTION_HASH, zeroValue, TRANSACTION_HASH_2, value);
        BlockcypherAddressTransactionsDto blockcypherTransactionDto =
                objectMapper.readValue(json, BlockcypherAddressTransactionsDto.class);
        assertThat(blockcypherTransactionDto.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS)
                .getTransactionHashes()).containsExactly(TRANSACTION_HASH_2);
    }

    @Test
    void deserialization_lowest_completed_block_height() throws Exception {
        int height1 = 651_522;
        int height2 = 648_648;
        String json = """
                {
                   "address": "xxx",
                   "txrefs": [
                     {
                       "tx_hash": "abc",
                       "block_height": %d,
                       "value": 1
                     },
                     {
                       "tx_hash": "cde",
                       "block_height": %d,
                       "value": 1
                     }
                   ]
                }""".formatted(height1, height2);
        BlockcypherAddressTransactionsDto blockcypherTransactionDto =
                objectMapper.readValue(json, BlockcypherAddressTransactionsDto.class);
        assertThat(blockcypherTransactionDto.getLowestCompletedBlockHeight()).isEqualTo(height2);
    }

    @Test
    void deserialization_has_more() throws Exception {
        String json = """
                {
                   "address": "%s",
                   "txrefs": [],
                   "hasMore": true
                }""".formatted(ADDRESS);
        BlockcypherAddressTransactionsDto blockcypherTransactionDto =
                objectMapper.readValue(json, BlockcypherAddressTransactionsDto.class);
        assertThat(blockcypherTransactionDto.isIncomplete()).isTrue();
    }

    @Test
    void deserialization_lowest_completed_block_height_no_transaction() throws Exception {
        String json = """
                {
                   "address": "%s"
                }""".formatted(ADDRESS);
        BlockcypherAddressTransactionsDto blockcypherTransactionDto =
                objectMapper.readValue(json, BlockcypherAddressTransactionsDto.class);
        assertThat(blockcypherTransactionDto.getLowestCompletedBlockHeight()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void combine() {
        BlockcypherAddressTransactionsDto combined =
                ADDRESS_DETAILS_INCOMPLETE.combine(ADDRESS_DETAILS_SECOND_PART);
        assertThat(combined.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS)).isEqualTo(ADDRESS_TRANSACTIONS);
    }
}
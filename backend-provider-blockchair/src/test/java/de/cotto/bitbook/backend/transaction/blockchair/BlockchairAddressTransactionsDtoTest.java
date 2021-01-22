package de.cotto.bitbook.backend.transaction.blockchair;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.deserialization.TestObjectMapper;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.transaction.blockchair.BlockchairAddressTransactionsFixtures.BLOCKCHAIR_ADDRESS_DETAILS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BlockchairAddressTransactionsDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void toModel() {
        assertThat(BLOCKCHAIR_ADDRESS_DETAILS.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization() throws Exception {
        String json = """
                {
                    "data": {
                        "%s": {
                             "address": {
                                 "transaction_count": 2
                             },
                             "transactions": [
                                "%s",
                                "%s"
                             ]
                         }
                     }
                 }""".formatted(ADDRESS, TRANSACTION_HASH, TRANSACTION_HASH_2);
        BlockchairAddressTransactionsDto blockchairTransactionDto =
                objectMapper.readValue(json, BlockchairAddressTransactionsDto.class);
        assertThat(blockchairTransactionDto.toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS))
                .isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void deserialization_wrong_address() {
        String json = """
                {
                    "data": {
                        "%s": {
                             "address": {
                                 "transaction_count": 2
                             },
                             "transactions": [
                                "%s",
                                "%s"
                             ]
                         }
                     }
                 }""".formatted("xxx", TRANSACTION_HASH, TRANSACTION_HASH_2);
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                objectMapper.readValue(json, BlockchairAddressTransactionsDto.class)
                        .toModel(LAST_CHECKED_AT_BLOCK_HEIGHT, ADDRESS)
        );
    }

    @Test
    void deserialization_number_does_not_match() {
        String json = """
                {
                    "data": {
                        "%s": {
                             "address": {
                                 "transaction_count": 3
                             },
                             "transactions": [
                                "%s",
                                "%s"
                             ]
                         }
                     }
                 }""".formatted(ADDRESS, TRANSACTION_HASH, TRANSACTION_HASH_2);
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
            objectMapper.readValue(json, BlockchairAddressTransactionsDto.class)
        );
    }
}
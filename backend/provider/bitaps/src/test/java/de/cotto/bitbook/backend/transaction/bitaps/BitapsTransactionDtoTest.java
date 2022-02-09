package de.cotto.bitbook.backend.transaction.bitaps;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.deserialization.TestObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;

import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_VALUE_1;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_VALUE_2;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_VALUE_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_VALUE_2;
import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.model.TransactionFixtures.FEES;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BitapsTransactionDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void deserialization() throws Exception {
        long formattedDateTime = DATE_TIME.atOffset(ZoneOffset.UTC).toInstant().getEpochSecond();
        String json = """
                {
                  "data": {
                    "txId": "%s",
                    "vIn": {
                        "0": {
                            "amount": %d,
                            "address": "%s"
                        },
                        "1": {
                            "amount": %d,
                            "address": "%s"
                        }
                    },
                    "vOut": {
                      "0": {
                        "value": %d,
                        "address": "%s"
                      },
                      "1": {
                        "value": %d,
                        "address": "%s"
                      }
                    },
                    "time": %d,
                    "blockTime": 123,
                    "fee": %d,
                    "blockHeight": %d
                  }
                  }""".formatted(
                TRANSACTION_HASH,
                INPUT_VALUE_1.getSatoshis(), INPUT_ADDRESS_1,
                INPUT_VALUE_2.getSatoshis(), INPUT_ADDRESS_2,
                OUTPUT_VALUE_1.getSatoshis(), OUTPUT_ADDRESS_1,
                OUTPUT_VALUE_2.getSatoshis(), OUTPUT_ADDRESS_2,
                formattedDateTime, FEES.getSatoshis(), BLOCK_HEIGHT
        );
        BitapsTransactionDto bitapsTransactionDto =
                objectMapper.readValue(json, BitapsTransactionDto.class);
        assertThat(bitapsTransactionDto.toModel()).isEqualTo(TRANSACTION);
    }

    @Test
    void deserialization_coinbase_transaction() throws Exception {
        long formattedDateTime = DATE_TIME.atOffset(ZoneOffset.UTC).toInstant().getEpochSecond();
        String json = """
                {
                  "data": {
                    "txId": "%s",
                    "vIn": {
                        "0": {
                            "txId": "0000000000000000000000000000000000000000000000000000000000000000"
                        }
                    },
                    "vOut": {
                      "0": {
                        "value": 100,
                        "address": "abc"
                      }
                    },
                    "time": %d,
                    "blockTime": 123,
                    "fee": 0,
                    "blockHeight": %d,
                    "coinbase": true
                  }
                  }""".formatted(TRANSACTION_HASH, formattedDateTime, BLOCK_HEIGHT);
        BitapsTransactionDto bitapsTransactionDto =
                objectMapper.readValue(json, BitapsTransactionDto.class);
        assertThat(bitapsTransactionDto.toModel().getOutputs()).hasSize(1);
    }

    @Test
    void deserialization_coinbase_transaction_unexpected_txId() {
        long formattedDateTime = DATE_TIME.atOffset(ZoneOffset.UTC).toInstant().getEpochSecond();
        String json = """
                {
                  "data": {
                    "txId": "%s",
                    "vIn": {
                        "0": {
                            "txId": "x"
                        }
                    },
                    "vOut": {
                      "0": {
                        "value": 100,
                        "address": "abc"
                      }
                    },
                    "time": %d,
                    "blockTime": 123,
                    "fee": 0,
                    "blockHeight": %d,
                    "coinbase": true
                  }
                  }""".formatted(TRANSACTION_HASH, formattedDateTime, BLOCK_HEIGHT);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                objectMapper.readValue(json, BitapsTransactionDto.class).toModel()
        );
    }
}
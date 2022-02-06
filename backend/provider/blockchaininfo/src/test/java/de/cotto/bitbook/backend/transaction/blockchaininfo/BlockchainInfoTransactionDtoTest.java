package de.cotto.bitbook.backend.transaction.blockchaininfo;

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
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BlockchainInfoTransactionDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void deserialization() throws Exception {
        long formattedDateTime = DATE_TIME.atOffset(ZoneOffset.UTC).toInstant().getEpochSecond();
        String json = """
                {
                    "hash": "%s",
                    "inputs": [
                        {
                            "prev_out": {
                                "value": %d,
                                "addr": "%s"
                            }
                        },
                        {
                            "prev_out": {
                                "value": %d,
                                "addr": "%s"
                            }
                        }
                    ],
                    "out": [
                      {
                        "value": %d,
                        "addr": "%s"
                      },
                      {
                        "value": %d,
                        "addr": "%s"
                      }
                    ],
                    "time": %d,
                    "fee": %d,
                    "block_height": %d
                  }""".formatted(
                TRANSACTION_HASH,
                INPUT_VALUE_1.getSatoshis(), INPUT_ADDRESS_1,
                INPUT_VALUE_2.getSatoshis(), INPUT_ADDRESS_2,
                OUTPUT_VALUE_1.getSatoshis(), OUTPUT_ADDRESS_1,
                OUTPUT_VALUE_2.getSatoshis(), OUTPUT_ADDRESS_2,
                formattedDateTime, FEES.getSatoshis(), BLOCK_HEIGHT
        );
        BlockchainInfoTransactionDto blockchainInfoTransactionDto =
                objectMapper.readValue(json, BlockchainInfoTransactionDto.class);
        assertThat(blockchainInfoTransactionDto.toModel()).isEqualTo(TRANSACTION);
    }

    @Test
    void deserialization_no_input() throws Exception {
        long formattedDateTime = DATE_TIME.atOffset(ZoneOffset.UTC).toInstant().getEpochSecond();
        String json = """
                {
                    "hash": "%s",
                    "inputs": [],
                    "out": [
                      {
                        "value": %d,
                        "addr": "%s"
                      },
                      {
                        "value": %d,
                        "addr": "%s"
                      }
                    ],
                    "time": %d,
                    "fee": %d,
                    "block_height": %d
                  }""".formatted(
                TRANSACTION_HASH,
                OUTPUT_VALUE_1.getSatoshis(), OUTPUT_ADDRESS_1,
                OUTPUT_VALUE_2.getSatoshis(), OUTPUT_ADDRESS_2,
                formattedDateTime, FEES.getSatoshis(), BLOCK_HEIGHT
        );
        BlockchainInfoTransactionDto blockchainInfoTransactionDto =
                objectMapper.readValue(json, BlockchainInfoTransactionDto.class);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(blockchainInfoTransactionDto::toModel);
    }

    @Test
    void deserialization_coinbase_transaction() throws Exception {
        long formattedDateTime = DATE_TIME.atOffset(ZoneOffset.UTC).toInstant().getEpochSecond();
        String json = """
                {
                    "hash": "%s",
                    "inputs": [
                        {
                            "index": 0,
                            "prev_out": null
                        }
                    ],
                    "out": [
                      {
                        "value": 100,
                        "addr": "abc"
                      }
                    ],
                    "time": %d,
                    "fee": 0,
                    "block_height": %d
                  }""".formatted(TRANSACTION_HASH, formattedDateTime, BLOCK_HEIGHT);
        BlockchainInfoTransactionDto blockchainInfoTransactionDto =
                objectMapper.readValue(json, BlockchainInfoTransactionDto.class);
        assertThat(blockchainInfoTransactionDto.toModel().getOutputs()).hasSize(1);
    }

    @Test
    void deserialization_coinbase_transaction_unexpected_index()  {
        long formattedDateTime = DATE_TIME.atOffset(ZoneOffset.UTC).toInstant().getEpochSecond();
        String json = """
                {
                    "hash": "%s",
                    "inputs": [
                        {
                            "index": 1,
                            "prev_out": null
                        }
                    ],
                    "out": [
                      {
                        "value": 100,
                        "addr": "abc"
                      }
                    ],
                    "time": %d,
                    "fee": 0,
                    "block_height": %d
                  }""".formatted(TRANSACTION_HASH, formattedDateTime, BLOCK_HEIGHT);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                objectMapper.readValue(json, BlockchainInfoTransactionDto.class).toModel()
        );
    }

    @Test
    void deserialization_coinbase_transaction_unexpected_prev_out() {
        long formattedDateTime = DATE_TIME.atOffset(ZoneOffset.UTC).toInstant().getEpochSecond();
        String json = """
                {
                    "hash": "%s",
                    "inputs": [
                        {
                            "index": 0,
                            "prev_out": {
                                "value": 123,
                                "addr": "abc"
                            }
                        }
                    ],
                    "out": [
                      {
                        "value": 100,
                        "addr": "abc"
                      }
                    ],
                    "time": %d,
                    "fee": 0,
                    "block_height": %d
                  }""".formatted(TRANSACTION_HASH, formattedDateTime, BLOCK_HEIGHT);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                objectMapper.readValue(json, BlockchainInfoTransactionDto.class).toModel()
        );
    }

    @Test
    void deserialization_coinbase_transaction_missing_index() {
        long formattedDateTime = DATE_TIME.atOffset(ZoneOffset.UTC).toInstant().getEpochSecond();
        String json = """
                {
                    "hash": "%s",
                    "inputs": [
                        {
                            "prev_out": null
                        }
                    ],
                    "out": [
                      {
                        "value": 100,
                        "addr": "abc"
                      }
                    ],
                    "time": %d,
                    "fee": 0,
                    "block_height": %d
                  }""".formatted(TRANSACTION_HASH, formattedDateTime, BLOCK_HEIGHT);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                objectMapper.readValue(json, BlockchainInfoTransactionDto.class).toModel()
        );
    }

    @Test
    void deserialization_coinbase_transaction_missing_prev_out() {
        long formattedDateTime = DATE_TIME.atOffset(ZoneOffset.UTC).toInstant().getEpochSecond();
        String json = """
                {
                    "hash": "%s",
                    "inputs": [
                        {
                            "index": 0
                        }
                    ],
                    "out": [
                      {
                        "value": 100,
                        "addr": "abc"
                      }
                    ],
                    "time": %d,
                    "fee": 0,
                    "block_height": %d
                  }""".formatted(TRANSACTION_HASH, formattedDateTime, BLOCK_HEIGHT);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                objectMapper.readValue(json, BlockchainInfoTransactionDto.class).toModel()
        );
    }
}
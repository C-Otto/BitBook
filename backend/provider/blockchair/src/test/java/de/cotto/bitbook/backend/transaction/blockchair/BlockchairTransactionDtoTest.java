package de.cotto.bitbook.backend.transaction.blockchair;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.deserialization.TestObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static de.cotto.bitbook.backend.model.Chain.BTC;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class BlockchairTransactionDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void deserialization() throws Exception {
        String formattedDate = DATE_TIME.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String formattedTime = DATE_TIME.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_TIME);
        String formattedDateTime = formattedDate + " " + formattedTime;
        String json = """
                {
                  "data": {
                    "%s": {
                      "transaction": {
                        "block_id": %d,
                        "hash": "%s",
                        "time": "%s",
                        "fee": %d
                      },
                      "inputs": [
                          {
                            "value": %d,
                            "recipient": "%s"
                          },
                          {
                            "value": %d,
                            "recipient": "%s"
                          }
                      ],
                      "outputs": [
                        {
                          "value": %d,
                          "recipient": "%s"
                        },
                        {
                          "value": %d,
                          "recipient": "%s"
                        }
                      ]
                    }
                  }
                }""".formatted(
                TRANSACTION_HASH, BLOCK_HEIGHT, TRANSACTION_HASH, formattedDateTime, FEES.getSatoshis(),
                INPUT_VALUE_1.getSatoshis(), INPUT_ADDRESS_1,
                INPUT_VALUE_2.getSatoshis(), INPUT_ADDRESS_2,
                OUTPUT_VALUE_1.getSatoshis(), OUTPUT_ADDRESS_1,
                OUTPUT_VALUE_2.getSatoshis(), OUTPUT_ADDRESS_2);
        BlockchairTransactionDto blockchairTransactionDto =
                objectMapper.readValue(json, BlockchairTransactionDto.class);
        assertThat(blockchairTransactionDto.toModel(BTC)).isEqualTo(TRANSACTION);
    }

    @Test
    void deserialization_coinbase_transaction() throws Exception {
        String json = """
                {
                  "data": {
                    "%s": {
                      "transaction": {
                        "block_id": %d,
                        "hash": "%s",
                        "time": "2021-03-25 21:28:25",
                        "fee": %d,
                        "is_coinbase": true
                      },
                      "inputs": [],
                      "outputs": [
                        {
                          "value": %d,
                          "recipient": "%s"
                        }
                      ]
                    }
                  }
                }""".formatted(
                TRANSACTION_HASH, BLOCK_HEIGHT, TRANSACTION_HASH, FEES.getSatoshis(),
                OUTPUT_VALUE_1.getSatoshis(), OUTPUT_ADDRESS_1);
        BlockchairTransactionDto blockchairTransactionDto =
                objectMapper.readValue(json, BlockchairTransactionDto.class);
        assertThat(blockchairTransactionDto.toModel(BTC).getOutputs()).hasSize(1);
    }

    @Test
    void deserialization_coinbase_transaction_wrong_value() {
        String json = """
                {
                  "data": {
                    "%s": {
                      "transaction": {
                        "block_id": %d,
                        "hash": "%s",
                        "time": "2021-03-25 21:28:25",
                        "fee": %d,
                        "is_coinbase": false
                      },
                      "inputs": [],
                      "outputs": [
                        {
                          "value": %d,
                          "recipient": "%s"
                        }
                      ]
                    }
                  }
                }""".formatted(
                TRANSACTION_HASH, BLOCK_HEIGHT, TRANSACTION_HASH, FEES.getSatoshis(),
                OUTPUT_VALUE_1.getSatoshis(), OUTPUT_ADDRESS_1);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                objectMapper.readValue(json, BlockchairTransactionDto.class).toModel(BTC)
        );
    }

    @Test
    void deserialization_multisig_output() {
        // example: 4c1df235ffd7642008989422aee5255e6312b4172b55d94e328fa99e99d727c7
        String formattedDate = DATE_TIME.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String formattedTime = DATE_TIME.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_TIME);
        String formattedDateTime = formattedDate + " " + formattedTime;
        String json = """
                {
                  "data": {
                    "xxx": {
                      "transaction": {
                        "block_id": 123,
                        "hash": "xxx",
                        "time": "%s",
                        "fee": 123
                      },
                      "inputs": [],
                      "outputs": [
                        {
                          "value": 1,
                          "recipient": "%s"
                        }
                      ]
                    }
                  }
                }""".formatted(
                formattedDateTime, "m-c1e34ed161308769d9bc664c56cb0a36");
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                objectMapper.readValue(json, BlockchairTransactionDto.class)
        );
    }

    @Test
    void deserialization_zero_output_with_d_address() throws Exception {
        // example: ef381fefedcd61db670b592dd433e6116f26eae244ecd4364bdabc4376f9c36a
        String json = """
                {
                  "data": {
                    "xxx": {
                      "transaction": {
                        "block_id": 123,
                        "hash": "xxx",
                        "time": "2016-02-19 13:17:17",
                        "fee": 0
                      },
                      "inputs": [],
                      "outputs": [
                        {
                          "value": 0,
                          "recipient": "d-db016c612be15c26723a4ff98244fab6"
                        }
                      ]
                    }
                  }
                }""";
        BlockchairTransactionDto blockchairTransactionDto =
                objectMapper.readValue(json, BlockchairTransactionDto.class);
        assertThat(blockchairTransactionDto.toModel(BTC).getOutputs()).isEmpty();
    }
}
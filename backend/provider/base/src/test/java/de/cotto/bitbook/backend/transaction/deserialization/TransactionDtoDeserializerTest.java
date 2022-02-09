package de.cotto.bitbook.backend.transaction.deserialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Input;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_VALUE_1;
import static de.cotto.bitbook.backend.model.InputFixtures.INPUT_VALUE_2;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_VALUE_1;
import static de.cotto.bitbook.backend.model.OutputFixtures.OUTPUT_VALUE_2;
import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.DATE_TIME_EPOCH_SECONDS;
import static de.cotto.bitbook.backend.model.TransactionFixtures.FEES;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TransactionDtoDeserializerTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void deserialization() throws Exception {
        String json = """
                {
                  "blockheight": %d,
                  "this-is-the-hash": "%s",
                  "fees": %d,
                  "received": %d,
                  "inputs": [
                    {
                      "val": %d,
                      "addr": "%s"
                    },
                    {
                      "val": %d,
                      "addr": "%s"
                    }
                  ],
                  "outputs": [
                    {
                      "val": %d,
                      "addr": "%s"
                    },
                    {
                      "val": %d,
                      "addr": "%s"
                    }
                  ]
                }""".formatted(
                BLOCK_HEIGHT, TRANSACTION_HASH, FEES.getSatoshis(), DATE_TIME_EPOCH_SECONDS,
                INPUT_VALUE_1.getSatoshis(), INPUT_ADDRESS_1,
                INPUT_VALUE_2.getSatoshis(), INPUT_ADDRESS_2,
                OUTPUT_VALUE_1.getSatoshis(), OUTPUT_ADDRESS_1,
                OUTPUT_VALUE_2.getSatoshis(), OUTPUT_ADDRESS_2
        );
        TestableTransactionDto transactionDto =
                objectMapper.readValue(json, TestableTransactionDto.class);
        assertThat(transactionDto.toModel()).isEqualTo(TRANSACTION);
    }

    @Test
    void duplicate_input_must_be_counted_twice() throws Exception {
        String json = """
                {
                  "hidden_in_here": {
                    "xxx": {
                      "blockheight": 123,
                      "this-is-the-hash": "xxx",
                      "fees": 200,
                      "received": 123,
                      "inputs": [
                        {
                          "val": 100,
                          "addr": "xxx"
                        },
                        {
                          "val": 100,
                          "addr": "xxx"
                        }
                      ],
                      "outputs": []
                    }
                  }
                }""";
        TestableTransactionDto transactionDto =
                objectMapper.readValue(json, TestableTransactionDto.class);
        Input input1 = new Input(Coins.ofSatoshis(100), new Address("xxx"));
        Input input2 = new Input(Coins.ofSatoshis(100), new Address("xxx"));
        assertThat(transactionDto.toModel().getInputs()).containsExactly(input1, input2);
    }

    @Test
    void unsupported() {
        String json = """
                {
                  "unsupported": 123,
                  "hidden_in_here": {
                    "xxx": {
                      "blockheight": 123,
                      "this-is-the-hash": "xxx",
                      "fees": 200,
                      "received": 123,
                      "inputs": [
                        {
                          "val": 100,
                          "addr": "xxx"
                        }
                      ],
                      "outputs": []
                    }
                  }
                }""";
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                objectMapper.readValue(json, TestableTransactionDto.class)
        );
    }

    @Test
    void deserialization_nested_transactions() throws Exception {
        String json = """
                {
                  "hidden_in_here": {
                    "xxx": {
                      "blockheight": %d,
                      "this-is-the-hash": "%s",
                      "fees": 1,
                      "received": %d,
                      "inputs": [
                        {
                          "val": 3,
                          "addr": "in"
                        }
                      ],
                      "outputs": [
                        {
                          "val": 2,
                          "addr": "out"
                        }
                      ]
                    }
                  }
                }""".formatted(BLOCK_HEIGHT, TRANSACTION_HASH, DATE_TIME_EPOCH_SECONDS);
        TestableTransactionDto transactionDto =
                objectMapper.readValue(json, TestableTransactionDto.class);
        assertThat(transactionDto.toModel().getOutputs()).hasSize(1);
    }

    @Test
    void deserialization_nested_details() throws Exception {
        String json = """
                {
                    "details": {
                          "blockheight": %d,
                          "this-is-the-hash": "%s",
                          "fees": 5,
                          "received": %d
                      },
                      "inputs": [
                        {
                          "val": 6,
                          "addr": "in"
                        }
                      ],
                      "outputs": [
                        {
                          "val": 1,
                          "addr": "out"
                        }
                      ]
                    }
                }""".formatted(BLOCK_HEIGHT, TRANSACTION_HASH, DATE_TIME_EPOCH_SECONDS);
        TestableTransactionDto transactionDto =
                objectMapper.readValue(json, TestableTransactionDto.class);
        assertThat(transactionDto.toModel().getOutputs()).hasSize(1);
    }

    @Test
    void coinbase_input() throws Exception {
        String json = """
                {
                      "blockheight": %d,
                      "this-is-the-hash": "%s",
                      "fees": %d,
                      "received": %d,
                      "coinbase": "yessir",
                      "outputs": [
                        {
                          "val": %d,
                          "addr": "%s"
                        },
                        {
                          "val": %d,
                          "addr": "%s"
                        }
                      ]
                    }
                }""".formatted(
                BLOCK_HEIGHT, TRANSACTION_HASH, FEES.getSatoshis(), DATE_TIME_EPOCH_SECONDS,
                OUTPUT_VALUE_1.getSatoshis(), OUTPUT_ADDRESS_1,
                OUTPUT_VALUE_2.getSatoshis(), OUTPUT_ADDRESS_2
        );
        TestableTransactionDto transactionDto =
                objectMapper.readValue(json, TestableTransactionDto.class);
        assertThat(transactionDto.toModel().getInputs()).hasSize(1);
    }
}
package de.cotto.bitbook.backend.transaction.btccom;

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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class BtcComTransactionDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void deserialization() throws Exception {
        long formattedDateTime = DATE_TIME.atOffset(ZoneOffset.UTC).toInstant().getEpochSecond();
        String json = """
                {
                  "data": {
                    "block_height": %d,
                    "block_time": %d,
                    "fee": %d,
                    "hash": "%s",
                    "inputs_count": 2,
                    "is_coinbase": false,
                    "outputs_count": 2,
                    "inputs": [
                      {
                        "prev_addresses": [
                          "%s"
                        ],
                        "prev_value": %d,
                        "prev_type": "P2PKH"
                      },
                      {
                        "prev_addresses": [
                          "%s"
                        ],
                        "prev_value": %d,
                        "prev_type": "P2PKH"
                      }
                    ],
                    "outputs": [
                      {
                        "addresses": [
                          "%s"
                        ],
                        "value": %d,
                        "type": "P2PKH"
                      },
                      {
                        "addresses": [
                          "%s"
                        ],
                        "value": %d,
                        "type": "P2PKH"
                      }
                    ]
                  }
                }""".formatted(
                BLOCK_HEIGHT, formattedDateTime, FEES.getSatoshis(), TRANSACTION_HASH,
                INPUT_ADDRESS_1, INPUT_VALUE_1.getSatoshis(),
                INPUT_ADDRESS_2, INPUT_VALUE_2.getSatoshis(),
                OUTPUT_ADDRESS_1, OUTPUT_VALUE_1.getSatoshis(),
                OUTPUT_ADDRESS_2, OUTPUT_VALUE_2.getSatoshis());
        BtcComTransactionDto btcComTransactionDto =
                objectMapper.readValue(json, BtcComTransactionDto.class);
        assertThat(btcComTransactionDto.toModel()).isEqualTo(TRANSACTION);
    }

    @Test
    void deserialization_inputs_count_does_not_match() {
        String json = """
                {
                  "data": {
                    "block_height": 123,
                    "block_time": 123,
                    "fee": 123,
                    "hash": "abc",
                    "inputs_count": 1,
                    "is_coinbase": false,
                    "outputs_count": 2,
                    "inputs": [
                      {
                        "prev_addresses": [
                          "abc"
                        ],
                        "prev_value": 123,
                        "prev_type": "P2PKH"
                      },
                      {
                        "prev_addresses": [
                          "def"
                        ],
                        "prev_value": 456,
                        "prev_type": "P2PKH"
                      }
                    ],
                    "outputs": [
                      {
                        "addresses": [
                          "xyz"
                        ],
                        "value": 999,
                        "type": "P2PKH"
                      },
                      {
                        "addresses": [
                          "ggg"
                        ],
                        "value": 12,
                        "type": "P2PKH"
                      }
                    ]
                  }
                }""";
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                objectMapper.readValue(json, BtcComTransactionDto.class)
        );
    }

    @Test
    void deserialization_outputs_count_does_not_match() {
        long formattedDateTime = DATE_TIME.atOffset(ZoneOffset.UTC).toInstant().getEpochSecond();
        String json = """
                {
                  "data": {
                    "block_height": %d,
                    "block_time": %d,
                    "fee": %d,
                    "hash": "%s",
                    "inputs_count": 2,
                    "is_coinbase": false,
                    "outputs_count": 1,
                    "inputs": [
                      {
                        "prev_addresses": [
                          "%s"
                        ],
                        "prev_value": %d,
                        "prev_type": "P2PKH"
                      },
                      {
                        "prev_addresses": [
                          "%s"
                        ],
                        "prev_value": %d,
                        "prev_type": "P2PKH"
                      }
                    ],
                    "outputs": [
                      {
                        "addresses": [
                          "%s"
                        ],
                        "value": %d,
                        "type": "P2PKH"
                      },
                      {
                        "addresses": [
                          "%s"
                        ],
                        "value": %d,
                        "type": "P2PKH"
                      }
                    ]
                  }
                }""".formatted(
                BLOCK_HEIGHT, formattedDateTime, FEES.getSatoshis(), TRANSACTION_HASH,
                INPUT_ADDRESS_1, INPUT_VALUE_1.getSatoshis(),
                INPUT_ADDRESS_2, INPUT_VALUE_2.getSatoshis(),
                OUTPUT_ADDRESS_1, OUTPUT_VALUE_1.getSatoshis(),
                OUTPUT_ADDRESS_2, OUTPUT_VALUE_2.getSatoshis());
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                objectMapper.readValue(json, BtcComTransactionDto.class)
        );
    }

    @Test
    void deserialization_coinbase_transaction() throws Exception {
        String json = """
                {
                  "data": {
                    "block_height": 123,
                    "block_time": 123,
                    "fee": 123,
                    "hash": "abc",
                    "inputs_count": 1,
                    "is_coinbase": true,
                    "outputs_count": 1,
                    "inputs": [
                      {
                        "prev_addresses": [
                          ""
                        ],
                        "prev_value": 0,
                        "prev_type": "P2PKH"
                      }
                    ],
                    "outputs": [
                      {
                        "addresses": [
                          "abc"
                        ],
                        "value": 123,
                        "type": "P2PKH"
                      }
                    ]
                  }
                }""";
        BtcComTransactionDto btcComTransactionDto =
                objectMapper.readValue(json, BtcComTransactionDto.class);
        assertThat(btcComTransactionDto.toModel().getOutputs()).hasSize(1);
    }

    @Test
    void deserialization_coinbase_transaction_wrong_value() {
        String json = """
                {
                  "data": {
                    "block_height": 123,
                    "block_time": 123,
                    "fee": 0,
                    "hash": "abc",
                    "inputs_count": 1,
                    "is_coinbase": false,
                    "outputs_count": 1,
                    "inputs": [
                      {
                        "prev_addresses": [
                        ],
                        "prev_value": 0,
                        "prev_type": "P2PKH"
                      }
                    ],
                    "outputs": [
                      {
                        "addresses": [
                          "abc"
                        ],
                        "value": 123,
                        "type": "P2PKH"
                      }
                    ]
                  }
                }""";
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                objectMapper.readValue(json, BtcComTransactionDto.class).toModel()
        );
    }

    @Test
    void deserialization_is_coinbase_missing() {
        String json = """
                {
                  "data": {
                    "block_height": 123,
                    "block_time": 123,
                    "fee": 123,
                    "hash": "abc",
                    "inputs_count": 1,
                    "outputs_count": 1,
                    "inputs": [
                      {
                        "prev_addresses": [
                          ""
                        ],
                        "prev_value": 0,
                        "prev_type": "P2PKH"
                      }
                    ],
                    "outputs": [
                      {
                        "addresses": [
                          "abc"
                        ],
                        "value": 123,
                        "type": "P2PKH"
                      }
                    ]
                  }
                }""";
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                objectMapper.readValue(json, BtcComTransactionDto.class).toModel()
        );
    }

    @Test
    void deserialization_multisig_output() {
        // example: 4c1df235ffd7642008989422aee5255e6312b4172b55d94e328fa99e99d727c7
        String json = """
                {
                  "data": {
                    "block_height": 123,
                    "block_time": 123,
                    "fee": 123,
                    "hash": "abc",
                    "inputs_count": 0,
                    "is_coinbase": false,
                    "outputs_count": 1,
                    "inputs": [],
                    "outputs": [
                      {
                        "addresses": [
                          "abc"
                        ],
                        "value": 123
                      }
                    ]
                  }
                }""";
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                objectMapper.readValue(json, BtcComTransactionDto.class)
        );
    }

    @Test
    void deserialization_zero_output_with_null_data() throws Exception {
        // example: ef381fefedcd61db670b592dd433e6116f26eae244ecd4364bdabc4376f9c36a
        String json = """
                {
                  "data": {
                    "block_height": 123,
                    "block_time": 123,
                    "fee": 0,
                    "hash": "abc",
                    "inputs_count": 0,
                    "is_coinbase": false,
                    "outputs_count": 1,
                    "inputs": [],
                    "outputs": [
                      {
                        "addresses": [""],
                        "value": 0,
                        "type": "NULL_DATA"
                      }
                    ]
                  }
                }""";
        BtcComTransactionDto btcComTransactionDto =
                objectMapper.readValue(json, BtcComTransactionDto.class);
        assertThat(btcComTransactionDto.toModel().getOutputs()).isEmpty();
    }
}
package de.cotto.bitbook.backend.transaction.blockcypher;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.deserialization.TestObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_VALUE_1;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_VALUE_2;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_VALUE_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_VALUE_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.FEES;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BlockcypherTransactionDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void deserialization() throws Exception {
        String formattedDateTime = DATE_TIME.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        String json = """
                {
                  "block_height": %d,
                  "hash": "%s",
                  "fees": %d,
                  "confirmed": "2019-10-26T20:06:35Z",
                  "received": "%s",
                  "inputs": [
                    {
                      "output_value": %d,
                      "addresses": [
                        "%s"
                      ]
                    },
                    {
                      "output_value": %d,
                      "addresses": [
                        "%s"
                      ]
                    }
                  ],
                  "outputs": [
                    {
                      "value": %d,
                      "addresses": [
                        "%s"
                      ]
                    },
                    {
                      "value": %d,
                      "addresses": [
                        "%s"
                      ]
                    }
                  ]
                }""".formatted(
                BLOCK_HEIGHT, TRANSACTION_HASH, FEES.getSatoshis(), formattedDateTime,
                INPUT_VALUE_1.getSatoshis(), INPUT_ADDRESS_1,
                INPUT_VALUE_2.getSatoshis(), INPUT_ADDRESS_2,
                OUTPUT_VALUE_1.getSatoshis(), OUTPUT_ADDRESS_1,
                OUTPUT_VALUE_2.getSatoshis(), OUTPUT_ADDRESS_2
        );
        BlockcypherTransactionDto blockcypherTransactionDto =
                objectMapper.readValue(json, BlockcypherTransactionDto.class);
        assertThat(blockcypherTransactionDto.toModel()).isEqualTo(TRANSACTION);
    }

    @Test
    void deserialization_coinbase_transaction() throws Exception {
        String json = """
                {
                  "block_height": 123,
                  "hash": "abc",
                  "fees": 0,
                  "confirmed": "2019-10-26T20:06:35Z",
                  "received": "2019-10-26T20:06:35Z",
                  "inputs": [
                    {
                      "output_index": -1,
                      "script_type": "empty"
                    }
                  ],
                  "outputs": [
                    {
                      "value": 100,
                      "addresses": [
                        "abc"
                      ]
                    }
                  ]
                }""";
        BlockcypherTransactionDto blockcypherTransactionDto =
                objectMapper.readValue(json, BlockcypherTransactionDto.class);
        assertThat(blockcypherTransactionDto.toModel().getOutputs()).hasSize(1);
    }

    @Test
    void deserialization_coinbase_transaction_wrong_output_index() {
        String json = """
                {
                  "block_height": 123,
                  "hash": "abc",
                  "fees": 0,
                  "confirmed": "2019-10-26T20:06:35Z",
                  "received": "2019-10-26T20:06:35Z",
                  "inputs": [
                    {
                      "output_index": 0,
                      "script_type": "empty"
                    }
                  ],
                  "outputs": [
                    {
                      "value": 100,
                      "addresses": [
                        "abc"
                      ]
                    }
                  ]
                }""";
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                objectMapper.readValue(json, BlockcypherTransactionDto.class).toModel()
        );
    }

    @Test
    void deserialization_coinbase_transaction_wrong_script_type() {
        String json = """
                {
                  "block_height": 123,
                  "hash": "abc",
                  "fees": 0,
                  "confirmed": "2019-10-26T20:06:35Z",
                  "received": "2019-10-26T20:06:35Z",
                  "inputs": [
                    {
                      "output_index": -1,
                      "script_type": "something"
                    }
                  ],
                  "outputs": [
                    {
                      "value": 100,
                      "addresses": [
                        "abc"
                      ]
                    }
                  ]
                }""";
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                objectMapper.readValue(json, BlockcypherTransactionDto.class).toModel()
        );
    }

    @Test
    void deserialization_coinbase_transaction_missing_output_index() {
        String json = """
                {
                  "block_height": 123,
                  "hash": "abc",
                  "fees": 0,
                  "confirmed": "2019-10-26T20:06:35Z",
                  "received": "2019-10-26T20:06:35Z",
                  "inputs": [
                    {
                      "script_type": "empty"
                    }
                  ],
                  "outputs": [
                    {
                      "value": 100,
                      "addresses": [
                        "abc"
                      ]
                    }
                  ]
                }""";
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                objectMapper.readValue(json, BlockcypherTransactionDto.class).toModel()
        );
    }

    @Test
    void deserialization_coinbase_transaction_missing_script_type() {
        String json = """
                {
                  "block_height": 123,
                  "hash": "abc",
                  "fees": 0,
                  "confirmed": "2019-10-26T20:06:35Z",
                  "received": "2019-10-26T20:06:35Z",
                  "inputs": [
                    {
                      "output_index": -1
                    }
                  ],
                  "outputs": [
                    {
                      "value": 100,
                      "addresses": [
                        "abc"
                      ]
                    }
                  ]
                }""";
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                objectMapper.readValue(json, BlockcypherTransactionDto.class).toModel()
        );
    }

    @Test
    void deserialization_coinbase_transaction_no_input() {
        String json = """
                {
                  "block_height": 123,
                  "hash": "abc",
                  "fees": 0,
                  "confirmed": "2019-10-26T20:06:35Z",
                  "received": "2019-10-26T20:06:35Z",
                  "inputs": [],
                  "outputs": [
                    {
                      "value": 100,
                      "addresses": [
                        "abc"
                      ]
                    }
                  ]
                }""";
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                objectMapper.readValue(json, BlockcypherTransactionDto.class).toModel()
        );
    }

    @Test
    void deserialization_output_for_multisig_with_4_prefix() {
        // 4c1df235ffd7642008989422aee5255e6312b4172b55d94e328fa99e99d727c7
        String json = """
                {
                  "block_height": 123,
                  "hash": "xxx",
                  "fees": 123,
                  "received": "2019-10-26T20:06:35Z",
                  "inputs": [],
                  "outputs": [
                    {
                        "value": 1,
                        "addresses": [
                            "4HPpn8zwm5zLfSj9rvSfqRKTFMVtZuhdqT"
                        ]
                    }
                  ]
                }""";
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
                () -> objectMapper.readValue(json, BlockcypherTransactionDto.class)
        );
    }

    @Test
    void deserialization_has_additional_outputs() {
        String json = """
                {
                  "block_height": 123,
                  "hash": "xxx",
                  "fees": 0,
                  "received": "2019-10-26T20:06:35Z",
                  "inputs": [],
                  "outputs": [],
                  "next_outputs": "foo"
                }""";
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
                () -> objectMapper.readValue(json, BlockcypherTransactionDto.class)
        );
    }

    @Test
    void deserialization_has_additional_inputs() {
        String json = """
                {
                  "block_height": 123,
                  "hash": "xxx",
                  "fees": 0,
                  "received": "2019-10-26T20:06:35Z",
                  "inputs": [],
                  "outputs": [],
                  "next_inputs": "foo"
                }""";
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
                () -> objectMapper.readValue(json, BlockcypherTransactionDto.class)
        );
    }
}
package de.cotto.bitbook.backend.transaction.smartbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.deserialization.TestObjectMapper;
import org.junit.jupiter.api.Test;

import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_VALUE_1;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_VALUE_2;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_ADDRESS_2;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_VALUE_1;
import static de.cotto.bitbook.backend.transaction.model.OutputFixtures.OUTPUT_VALUE_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.DATE_TIME_EPOCH_SECONDS;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.FEES;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SmartbitTransactionDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void deserialization() throws Exception {
        String json = """
                {
                    "transaction": {
                        "hash": "%s",
                        "block": %d,
                        "time": %d,
                        "fee_int": %d,
                        "inputs": [
                            {
                                "value_int": %d,
                                "addresses": [
                                    "%s"
                                ]
                            },
                            {
                                "value_int": %d,
                                "addresses": [
                                    "%s"
                                ]
                            }
                        ],
                        "outputs": [
                            {
                                "addresses": [
                                    "%s"
                                ],
                                "value_int": %d,
                                "type": "pubkeyhash"
                            },
                            {
                                "addresses": [
                                    "%s"
                                ],
                                "value_int": %d
                            }
                        ]
                    }
                }
                """.formatted(
                TRANSACTION_HASH, BLOCK_HEIGHT, DATE_TIME_EPOCH_SECONDS, FEES.getSatoshis(),
                INPUT_VALUE_1.getSatoshis(), INPUT_ADDRESS_1,
                INPUT_VALUE_2.getSatoshis(), INPUT_ADDRESS_2,
                OUTPUT_ADDRESS_1, OUTPUT_VALUE_1.getSatoshis(),
                OUTPUT_ADDRESS_2, OUTPUT_VALUE_2.getSatoshis());
        SmartbitTransactionDto smartbitTransactionDto =
                objectMapper.readValue(json, SmartbitTransactionDto.class);
        assertThat(smartbitTransactionDto.toModel()).isEqualTo(TRANSACTION);
    }

    @Test
    void deserialization_coinbase_transaction() throws Exception {
        String json = """
                {
                    "transaction": {
                        "coinbase": true,
                        "hash": "%s",
                        "block": %d,
                        "time": %d,
                        "fee_int": %d,
                        "outputs": [
                            {
                                "addresses": [
                                    "%s"
                                ],
                                "value_int": %d,
                                "type": "pubkeyhash"
                            }
                        ]
                    }
                }
                """.formatted(
                TRANSACTION_HASH, BLOCK_HEIGHT, DATE_TIME_EPOCH_SECONDS, FEES.getSatoshis(),
                OUTPUT_ADDRESS_1, OUTPUT_VALUE_1.getSatoshis()
        );
        SmartbitTransactionDto smartbitTransactionDto =
                objectMapper.readValue(json, SmartbitTransactionDto.class);
        assertThat(smartbitTransactionDto.toModel().getOutputs()).hasSize(1);
    }

    @Test
    void deserialization_coinbase_transaction_wrong_value() {
        String json = """
                {
                    "transaction": {
                        "coinbase": false,
                        "hash": "%s",
                        "block": %d,
                        "time": %d,
                        "fee_int": %d,
                        "outputs": [
                            {
                                "addresses": [
                                    "%s"
                                ],
                                "value_int": %d,
                                "type": "pubkeyhash"
                            }
                        ]
                    }
                }
                """.formatted(
                TRANSACTION_HASH, BLOCK_HEIGHT, DATE_TIME_EPOCH_SECONDS, FEES.getSatoshis(),
                OUTPUT_ADDRESS_1, OUTPUT_VALUE_1.getSatoshis()
        );
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                objectMapper.readValue(json, SmartbitTransactionDto.class).toModel()
        );
    }

    @Test
    void deserialization_type_multisig() {
        // 2cf72b8c8516efac098cace3393c51104bd9830d03102562600f5abf20176dd5
        String json = """
                {
                    "transaction": {
                      "hash": "xxx",
                      "block": 123,
                      "time": 456,
                      "fee_int": 789,
                      "inputs": [],
                      "outputs": [
                        {
                          "value_int": 1,
                          "addresses": ["xxx"],
                          "type": "multisig"
                        }
                      ]
                    }
                  }""";
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
                () -> objectMapper.readValue(json, SmartbitTransactionDto.class)
        );
    }
}
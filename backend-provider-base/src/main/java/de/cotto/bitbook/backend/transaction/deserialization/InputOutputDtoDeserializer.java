package de.cotto.bitbook.backend.transaction.deserialization;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface InputOutputDtoDeserializer {
    List<OutputDto> getOutputs(JsonNode transactionNode);

    List<InputDto> getInputs(JsonNode transactionNode);
}

package de.cotto.bitbook.backend.transaction.deserialization;

import com.fasterxml.jackson.databind.JsonNode;
import de.cotto.bitbook.backend.transaction.model.Coins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class MultiAddressInputOutputDtoDeserializer implements InputOutputDtoDeserializer {

    private final String inputValueProperty;
    private final String outputValueProperty;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String inputAddressesProperty;
    private final String outputAddressesProperty;

    public MultiAddressInputOutputDtoDeserializer(
            String inputValueProperty,
            String outputValueProperty,
            String inputAddressesProperty,
            String outputAddressesProperty
    ) {
        this.outputValueProperty = outputValueProperty;
        this.inputValueProperty = inputValueProperty;
        this.inputAddressesProperty = inputAddressesProperty;
        this.outputAddressesProperty = outputAddressesProperty;
    }

    @Override
    public List<InputDto> getInputs(JsonNode transactionNode) {
        int expected = getExpectedNumberOfInputs(transactionNode);
        return getInputsOrOutputs(
                transactionNode,
                "inputs",
                inputValueProperty,
                inputAddressesProperty,
                expected,
                InputDto::new
        );
    }

    @Override
    public List<OutputDto> getOutputs(JsonNode transactionNode) {
        int expected = getExpectedNumberOfOutputs(transactionNode);
        return getInputsOrOutputs(
                transactionNode,
                "outputs",
                outputValueProperty,
                outputAddressesProperty,
                expected,
                OutputDto::new
        );
    }

    protected int getExpectedNumberOfInputs(JsonNode transactionNode) {
        return -1;
    }

    protected int getExpectedNumberOfOutputs(JsonNode transactionNode) {
        return -1;
    }

    private <T extends InputOutputDto> List<T> getInputsOrOutputs(
            JsonNode transactionNode,
            String inputOutputProperty,
            String valueProperty,
            String addressesProperty,
            int expected,
            BiFunction<Coins, String, T> inputOutputCreator
    ) {
        List<T> result = new ArrayList<>();
        JsonNode inputsOrOutputs = transactionNode.get(inputOutputProperty);
        if (inputsOrOutputs == null) {
            return result;
        }
        for (JsonNode inputOutputNode : inputsOrOutputs) {
            result.add(getInputOutputDto(inputOutputNode, valueProperty, addressesProperty, inputOutputCreator));
        }
        if (expected >= 0 && expected != result.size()) {
            throw new IllegalStateException("expected " + expected + " inputs/outputs");
        }
        return result;
    }

    private <T extends InputOutputDto> T getInputOutputDto(
            JsonNode inputOutputNode,
            String valueProperty,
            String addressesProperty,
            BiFunction<Coins, String, T> dtoCreator
    ) {
        long value;
        if (inputOutputNode.has(valueProperty)) {
            value = inputOutputNode.get(valueProperty).longValue();
        } else {
            value = 0L;
        }
        return dtoCreator.apply(Coins.ofSatoshis(value), getAddress(inputOutputNode, addressesProperty, value));
    }

    private String getAddress(JsonNode inputOutputNode, String addressesProperty, long value) {
        JsonNode addresses = inputOutputNode.get(addressesProperty);
        if (value == 0) {
            throwIfHasOutputAddress(addresses);
            return "";
        }
        return getValidatedAddress(inputOutputNode, addresses);
    }

    private String getValidatedAddress(JsonNode inputOutputNode, JsonNode addresses) {
        if (addresses == null || !addresses.isArray()) {
            throw logErrorAndGetException("expected addresses array");
        }
        int expectedNumberOfAddresses = 1;
        if (addresses.size() != expectedNumberOfAddresses) {
            throw logErrorAndGetException("expected exactly one address for input/output");
        }
        String address = addresses.get(0).textValue();
        if (isUnsupported(inputOutputNode, address)) {
            throw logErrorAndGetException("input/output is not supported");
        }
        return address;
    }

    private void throwIfHasOutputAddress(JsonNode addresses) {
        if (addresses != null && addresses.isArray() && !addresses.isEmpty() && !addresses.get(0).asText().isBlank()) {
            throw logErrorAndGetException("expected no address for 0 input/output value");
        }
    }

    protected boolean isUnsupported(JsonNode inputOutputNode, String address) {
        return false;
    }

    private IllegalStateException logErrorAndGetException(String errorMessage) {
        logger.error(errorMessage);
        return new IllegalStateException(errorMessage);
    }
}

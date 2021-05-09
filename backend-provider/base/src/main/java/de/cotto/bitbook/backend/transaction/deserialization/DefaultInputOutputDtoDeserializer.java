package de.cotto.bitbook.backend.transaction.deserialization;

import com.fasterxml.jackson.databind.JsonNode;
import de.cotto.bitbook.backend.transaction.model.Coins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class DefaultInputOutputDtoDeserializer implements InputOutputDtoDeserializer {
    private final List<String> inputsProperties;
    private final List<String> outputsProperties;
    private final String inputValueProperty;
    private final String outputValueProperty;
    private final String addressProperty;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public DefaultInputOutputDtoDeserializer(
            List<String> inputsProperties,
            List<String> outputsProperties,
            String inputValueProperty,
            String outputValueProperty,
            String addressProperty
    ) {
        this.inputsProperties = inputsProperties;
        this.outputsProperties = outputsProperties;
        this.inputValueProperty = inputValueProperty;
        this.outputValueProperty = outputValueProperty;
        this.addressProperty = addressProperty;
    }

    @Override
    public List<InputDto> getInputs(JsonNode transactionNode) {
        return getInputsOrOutputs(transactionNode, inputsProperties, InputDto::new, inputValueProperty);
    }

    @Override
    public List<OutputDto> getOutputs(JsonNode transactionNode) {
        return getInputsOrOutputs(transactionNode, outputsProperties, OutputDto::new, outputValueProperty);
    }

    private <T extends InputOutputDto> List<T> getInputsOrOutputs(
            JsonNode transactionNode,
            List<String> inputsOutputsProperties,
            BiFunction<Coins, String, T> creator, String valueProperty
    ) {
        List<T> result = new ArrayList<>();
        for (JsonNode inputOutputNode : getInputOutputNodes(transactionNode, inputsOutputsProperties)) {
            long value = getValue(inputOutputNode, valueProperty);
            JsonNode addressNode = inputOutputNode.get(addressProperty);

            String address;
            if (value > 0 && addressNode != null) {
                address = getAddress(addressNode);
            } else if (value == 0 && noAddress(addressNode)) {
                address = "";
            } else if (value > 0) {
                throw logErrorAndGetException("expected address");
            } else {
                throw logErrorAndGetException("expected input/output value");
            }
            result.add(creator.apply(Coins.ofSatoshis(value), address));
        }
        return result;
    }

    private List<JsonNode> getInputOutputNodes(JsonNode node, List<String> inputsOutputsProperties) {
        List<JsonNode> result = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode member : node) {
                result.addAll(getInputOutputNodes(member, inputsOutputsProperties));
            }
            return result;
        }
        if (inputsOutputsProperties.equals(List.of("*"))) {
            node.forEach(result::add);
            return result;
        }
        if (inputsOutputsProperties.isEmpty()) {
            return List.of(node);
        }
        JsonNode member = node.get(inputsOutputsProperties.get(0));
        if (member == null) {
            return List.of();
        }
        return getInputOutputNodes(member, inputsOutputsProperties.subList(1, inputsOutputsProperties.size()));
    }

    private boolean noAddress(JsonNode addressNode) {
        return addressNode == null
               || !addressNode.isTextual()
               || addressNode.textValue().startsWith("d-")
               || addressNode.textValue().isBlank();
    }

    private String getAddress(JsonNode addressNode) {
        if (noAddress(addressNode)) {
            throw logErrorAndGetException("expected address");
        }
        String address = addressNode.textValue();
        if (address.startsWith("m-")) {
            throw logErrorAndGetException("unknown address format");
        }
        return address;
    }

    private IllegalStateException logErrorAndGetException(String errorMessage) {
        logger.error(errorMessage);
        return new IllegalStateException(errorMessage);
    }

    private long getValue(JsonNode inputOutputNode, String valueProperty) {
        JsonNode node = inputOutputNode.get(valueProperty);
        if (node == null) {
            return 0;
        }
        return node.asLong();
    }
}

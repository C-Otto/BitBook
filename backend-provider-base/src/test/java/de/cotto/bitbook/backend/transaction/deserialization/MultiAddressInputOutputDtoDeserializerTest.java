package de.cotto.bitbook.backend.transaction.deserialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import de.cotto.bitbook.backend.transaction.model.Coins;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SuppressWarnings("CPD-START")
class MultiAddressInputOutputDtoDeserializerTest {
    private final TestableDeserializer deserializer = new TestableDeserializer();

    @Test
    void getInputsAndOutputs() throws JsonProcessingException {
        String json = "{\"inputs\": [" +
                      "{\"inputAddresses\": [\"aaa\"], \"inputValue\": 3}, " +
                      "{\"inputAddresses\": [\"bbb\"], \"inputValue\": 4}" +
                      "], " +
                      "\"outputs\": [" +
                      "{\"outputAddresses\": [\"outp1\"], \"outputValue\": 111}, " +
                      "{\"outputAddresses\": [\"outp2\"], \"outputValue\": 222}" +
                      "]}";
        JsonNode jsonNode = getJsonNode(json);
        assertThat(deserializer.getOutputs(jsonNode))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new OutputDto(Coins.ofSatoshis(111), "outp1"),
                new OutputDto(Coins.ofSatoshis(222), "outp2")
            );
        assertThat(deserializer.getInputs(jsonNode))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new InputDto(Coins.ofSatoshis(3), "aaa"),
                new InputDto(Coins.ofSatoshis(4), "bbb")
            );
    }

    @Nested
    class GetInputs {
        @Test
        void empty_no_node() throws JsonProcessingException {
            assertThat(get("{}")).isEmpty();
        }

        @Test
        void empty_array() throws JsonProcessingException {
            assertThat(get("{\"inputs\":[]}")).isEmpty();
        }

        @Test
        void mismatched_expected_number_of_inputs() {
            deserializer.setExpectedNumberOfInputs(1);
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"inputs\":[]}")
            );
        }

        @Test
        void matches_expected_number_of_inputs() throws JsonProcessingException {
            deserializer.setExpectedNumberOfInputs(0);
            assertThat(get("{\"inputs\":[]}")).isEmpty();
        }

        @Test
        void value_but_no_address() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"inputs\":[{\"inputValue\": 1}]}")
            );
        }

        @Test
        void no_value_and_no_addresses_property() throws JsonProcessingException {
            assertThat(get("{\"inputs\":[{\"inputValue\": 0}]}"))
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new InputDto(Coins.NONE, ""));
        }

        @Test
        void no_value_and_addresses_is_something_else() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"inputs\":[{\"inputValue\": 1, \"inputAddresses\": 5}]}")
            );
        }

        @Test
        void no_value_and_two_addresses() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"inputs\":[{\"inputValue\": 1, \"inputAddresses\": [\"a\", \"b\"]}]}")
            );
        }

        @Test
        void no_value_and_addresses_is_null() throws JsonProcessingException {
            assertThat(get("{\"inputs\":[{\"inputValue\": 0, \"inputAddresses\": null}]}"))
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new InputDto(Coins.NONE, ""));
        }

        @Test
        void no_value_and_addresses_is_empty() throws JsonProcessingException {
            assertThat(get("{\"inputs\":[{\"inputValue\": 0, \"inputAddresses\": []}]}"))
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new InputDto(Coins.NONE, ""));
        }

        @Test
        void no_value_and_only_address_is_blank() throws JsonProcessingException {
            assertThat(get("{\"inputs\":[{\"inputValue\": 0, \"inputAddresses\": [\" \"]}]}"))
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new InputDto(Coins.NONE, ""));
        }

        @Test
        void unsupported() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"inputs\":[{\"inputValue\": 1, \"inputAddresses\": [\"unsupported\"]}]}")
            );
        }

        @Test
        void address_but_no_value() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"inputs\":[{\"inputAddresses\": [\"xxx\"]}]}")
            );
        }

        @Test
        void getInputs() throws JsonProcessingException {
            String json = "{\"inputs\": [" +
                          "{\"inputAddresses\": [\"aaa\"], \"inputValue\": 100}," +
                          "{\"inputAddresses\": [\"bbb\"], \"inputValue\": 200}" +
                          "]}";
            assertThat(get(json))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(
                    new InputDto(Coins.ofSatoshis(100), "aaa"),
                    new InputDto(Coins.ofSatoshis(200), "bbb")
                );
        }

        private List<InputDto> get(String json) throws JsonProcessingException {
            return deserializer.getInputs(getJsonNode(json));
        }
    }

    @Nested
    class GetOutputs {
        @Test
        void empty_no_node() throws JsonProcessingException {
            assertThat(get("{}")).isEmpty();
        }

        @Test
        void empty_array() throws JsonProcessingException {
            assertThat(get("{\"outputs\":[]}")).isEmpty();
        }

        @Test
        void mismatched_expected_number_of_outputs() {
            deserializer.setExpectedNumberOfOutputs(1);
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"outputs\":[]}")
            );
        }

        @Test
        void matches_expected_number_of_outputs() throws JsonProcessingException {
            deserializer.setExpectedNumberOfOutputs(0);
            assertThat(get("{\"outputs\":[]}")).isEmpty();
        }

        @Test
        void value_but_no_address() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"outputs\":[{\"outputValue\": 1}]}")
            );
        }

        @Test
        void no_value_and_no_addresses_property() throws JsonProcessingException {
            assertThat(get("{\"outputs\":[{\"outputValue\": 0}]}"))
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new OutputDto(Coins.NONE, ""));
        }

        @Test
        void no_value_and_addresses_is_something_else() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"outputs\":[{\"outputValue\": 1, \"outputAddresses\": 5}]}")
            );
        }

        @Test
        void no_value_and_two_addresses() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"outputs\":[{\"outputValue\": 1, \"outputAddresses\": [\"a\", \"b\"]}]}")
            );
        }

        @Test
        void no_value_and_addresses_is_null() throws JsonProcessingException {
            assertThat(get("{\"outputs\":[{\"outputValue\": 0, \"outputAddresses\": null}]}"))
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new OutputDto(Coins.NONE, ""));
        }

        @Test
        void no_value_and_addresses_is_empty() throws JsonProcessingException {
            assertThat(get("{\"outputs\":[{\"outputValue\": 0, \"outputAddresses\": []}]}"))
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new OutputDto(Coins.NONE, ""));
        }

        @Test
        void no_value_and_only_address_is_blank() throws JsonProcessingException {
            assertThat(get("{\"outputs\":[{\"outputValue\": 0, \"outputAddresses\": [\" \"]}]}"))
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new OutputDto(Coins.NONE, ""));
        }

        @Test
        void unsupported() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"outputs\":[{\"outputValue\": 1, \"outputAddresses\": [\"unsupported\"]}]}")
            );
        }

        @Test
        void address_but_no_value() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"outputs\":[{\"outputAddresses\": [\"xxx\"]}]}")
            );
        }

        @Test
        void getOutputs() throws JsonProcessingException {
            String json = "{\"outputs\": [" +
                          "{\"outputAddresses\": [\"xxx\"], \"outputValue\": 1}," +
                          "{\"outputAddresses\": [\"yyy\"], \"outputValue\": 2}" +
                          "]}";
            assertThat(get(json)).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new OutputDto(Coins.ofSatoshis(1), "xxx"),
                    new OutputDto(Coins.ofSatoshis(2), "yyy")
            );
        }

        private List<OutputDto> get(String json) throws JsonProcessingException {
            return deserializer.getOutputs(getJsonNode(json));
        }
    }
    
    @SuppressWarnings("ReplaceNullCheck")
    private static class TestableDeserializer extends MultiAddressInputOutputDtoDeserializer {
        @Nullable
        private Integer expectedNumberOfInputs;

        @Nullable
        private Integer expectedNumberOfOutputs;

        public TestableDeserializer() {
            super(
                    "inputValue",
                    "outputValue",
                    "inputAddresses",
                    "outputAddresses"
            );
        }

        public void setExpectedNumberOfInputs(int expectedNumberOfInputs) {
            this.expectedNumberOfInputs = expectedNumberOfInputs;
        }

        public void setExpectedNumberOfOutputs(int expectedNumberOfOutputs) {
            this.expectedNumberOfOutputs = expectedNumberOfOutputs;
        }

        @Override
        public int getExpectedNumberOfInputs(JsonNode transactionNode) {
            if (expectedNumberOfInputs == null) {
                return super.getExpectedNumberOfInputs(transactionNode);
            }
            return expectedNumberOfInputs;
        }

        @Override
        public int getExpectedNumberOfOutputs(JsonNode transactionNode) {
            if (expectedNumberOfOutputs == null) {
                return super.getExpectedNumberOfOutputs(transactionNode);
            }
            return expectedNumberOfOutputs;
        }

        @Override
        protected boolean isUnsupported(JsonNode inputOutputNode, String address) {
            if ("unsupported".equals(address)) {
                return true;
            }
            return super.isUnsupported(inputOutputNode, address);
        }
    }

    private JsonNode getJsonNode(String json) throws JsonProcessingException {
        return DummyDeserializer.getJsonNode(json);
    }
}
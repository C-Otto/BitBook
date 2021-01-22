package de.cotto.bitbook.backend.transaction.deserialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Input;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DefaultInputOutputDtoDeserializerTest {
    private final TestableDeserializer deserializer = new TestableDeserializer();

    @Test
    void getInputsAndOutputs() throws JsonProcessingException {
        String json = "{\"inputs\": [" +
                      "{\"addr\": \"aaa\", \"inputValue\": 3}, " +
                      "{\"addr\": \"bbb\", \"inputValue\": 4}" +
                      "], " +
                      "\"outputs\": [" +
                      "{\"addr\": \"xxx\", \"outputValue\": 1}, " +
                      "{\"addr\": \"yyy\", \"outputValue\": 2}" +
                      "]}";
        JsonNode jsonNode = getJsonNode(json);
        assertThat(deserializer.getOutputs(jsonNode))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new OutputDto(Coins.ofSatoshis(1), "xxx"),
                new OutputDto(Coins.ofSatoshis(2), "yyy")
            );
        assertThat(deserializer.getInputs(jsonNode))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new InputDto(Coins.ofSatoshis(3), "aaa"),
                new InputDto(Coins.ofSatoshis(4), "bbb")
            );
    }

    @Test
    void getInputsAndOutputs_nested() throws JsonProcessingException {
        String json = "{\"a\": [" +
                      "{\"b\": {\"inputValue\": 1, \"addr\": \"abc\"}}, " +
                      "{\"b\": {\"inputValue\": 2, \"addr\": \"def\"}}" +
                      "], " +
                      "\"c\": [" +
                      "{\"outputValue\": 3, \"addr\": \"x\"}, " +
                      "{\"outputValue\": 4, \"addr\": \"y\"}" +
                      "]}";
        JsonNode jsonNode = getJsonNode(json);
        TestableDeserializer deserializer = new TestableDeserializer(List.of("a", "b"), List.of("c"));
        assertThat(deserializer.getInputs(jsonNode))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new InputDto(Coins.ofSatoshis(1), "abc"),
                new InputDto(Coins.ofSatoshis(2), "def")
            );
        assertThat(deserializer.getOutputs(jsonNode))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new OutputDto(Coins.ofSatoshis(3), "x"),
                new OutputDto(Coins.ofSatoshis(4), "y")
            );
    }

    @Test
    void getInputsAndOutputs_nested_associative() throws JsonProcessingException {
        String json = "{\"inp\":{\"0\":{\"inputValue\":1,\"addr\":\"a\"},\"1\":{\"inputValue\":2,\"addr\":\"b\"}}}";
        JsonNode jsonNode = getJsonNode(json);
        TestableDeserializer deserializer = new TestableDeserializer(List.of("inp", "*"), List.of());
        assertThat(deserializer.getInputs(jsonNode))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactly(
                new InputDto(Coins.ofSatoshis(1), "a"),
                new InputDto(Coins.ofSatoshis(2), "b")
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
        void value_but_no_address() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"inputs\":[{\"inputValue\": 1}]}")
            );
        }

        @Test
        void no_value_and_no_address_property() throws JsonProcessingException {
            assertThat(get("{\"inputs\":[{\"inputValue\": 0}]}"))
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new InputDto(Coins.NONE, ""));
        }

        @Test
        void no_value_and_address_is_something_else() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"inputs\":[{\"inputValue\": 1, \"addr\": 5}]}")
            );
        }

        @Test
        void no_value_and_address_is_array() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"inputs\":[{\"inputValue\": 1, \"addr\": [\"x\"]}]}")
            );
        }

        @Test
        void no_value_and_address_is_null() throws JsonProcessingException {
            assertThat(get("{\"inputs\":[{\"inputValue\": 0, \"addr\": null}]}"))
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new InputDto(Coins.NONE, ""));
        }

        @Test
        void no_value_and_address_is_blank() throws JsonProcessingException {
            assertThat(get("{\"inputs\":[{\"inputValue\": 0, \"addr\": \" \"}]}"))
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new InputDto(Coins.NONE, ""));
        }

        @Test
        void no_value_and_address_is_d_prefixed() throws JsonProcessingException {
            assertThat(get("{\"inputs\":[{\"inputValue\": 0, \"addr\": \"d-123\"}]}"))
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new InputDto(Coins.NONE, ""));
        }

        @Test
        void unsupported() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"inputs\":[{\"inputValue\": 1, \"addr\": \"m-123\"}]}")
            );
        }

        @Test
        void address_but_no_value() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"inputs\":[{\"addr\": \"xxx\"}]}")
            );
        }

        @Test
        void getInputs() throws JsonProcessingException {
            String json = "{\"inputs\": [" +
                          "{\"addr\": \"xxx\", \"inputValue\": 1}," +
                          "{\"addr\": \"yyy\", \"inputValue\": 2}" +
                          "]}";
            assertThat(get(json))
                .map(InputDto::toModel)
                .containsExactly(
                    new Input(Coins.ofSatoshis(1), "xxx"),
                    new Input(Coins.ofSatoshis(2), "yyy")
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
        void value_but_no_address() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"outputs\":[{\"outputValue\": 1}]}")
            );
        }

        @Test
        void no_value_and_no_address_property() throws JsonProcessingException {
            assertThat(get("{\"outputs\":[{\"outputValue\": 0}]}"))
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new OutputDto(Coins.NONE, ""));
        }

        @Test
        void no_value_and_address_is_something_else() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"outputs\":[{\"outputValue\": 1, \"addr\": 5}]}")
            );
        }

        @Test
        void no_value_and_address_is_array() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"outputs\":[{\"outputValue\": 1, \"addr\": [\"x\"]}]}")
            );
        }

        @Test
        void no_value_and_address_is_null() throws JsonProcessingException {
            assertThat(get("{\"outputs\":[{\"outputValue\": 0, \"addr\": null}]}"))
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new OutputDto(Coins.NONE, ""));
        }

        @Test
        void no_value_and_address_is_blank() throws JsonProcessingException {
            assertThat(get("{\"outputs\":[{\"outputValue\": 0, \"addr\": \" \"}]}"))
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new OutputDto(Coins.NONE, ""));
        }

        @Test
        void no_value_and_address_is_d_prefixed() throws JsonProcessingException {
            assertThat(get("{\"outputs\":[{\"outputValue\": 0, \"addr\": \"d-123\"}]}"))
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new OutputDto(Coins.NONE, ""));
        }

        @Test
        void unsupported() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"outputs\":[{\"outputValue\": 1, \"addr\": \"m-123\"}]}")
            );
        }

        @Test
        void address_but_no_value() {
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    get("{\"outputs\":[{\"addr\": \"xxx\"}]}")
            );
        }

        @Test
        void getOutputs() throws JsonProcessingException {
            String json = "{\"outputs\": [" +
                          "{\"addr\": \"xxx\", \"outputValue\": 1}," +
                          "{\"addr\": \"yyy\", \"outputValue\": 2}" +
                          "]}";
            assertThat(get(json))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(
                    new OutputDto(Coins.ofSatoshis(1), "xxx"),
                    new OutputDto(Coins.ofSatoshis(2), "yyy")
                );
        }

        private List<OutputDto> get(String json) throws JsonProcessingException {
            return deserializer.getOutputs(getJsonNode(json));
        }
    }

    private static class TestableDeserializer extends DefaultInputOutputDtoDeserializer {
        public TestableDeserializer() {
            super(List.of("inputs"), List.of("outputs"), "inputValue", "outputValue", "addr");
        }

        public TestableDeserializer(List<String> inputs, List<String> outputs) {
            super(inputs, outputs, "inputValue", "outputValue", "addr");
        }
    }

    private JsonNode getJsonNode(String json) throws JsonProcessingException {
        return DummyDeserializer.getJsonNode(json);
    }
}
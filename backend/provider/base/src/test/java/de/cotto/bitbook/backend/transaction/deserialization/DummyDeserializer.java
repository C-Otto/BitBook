package de.cotto.bitbook.backend.transaction.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

class DummyDeserializer extends JsonDeserializer<DummyDeserializer.TestableClassForDeserializer> {
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static JsonNode getJsonNode(String json) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(json, TestableClassForDeserializer.class).jsonNode;
    }

    public DummyDeserializer() {
        super();
    }

    @Override
    public TestableClassForDeserializer deserialize(
            JsonParser jsonParser,
            DeserializationContext deserializationContext
    ) throws IOException {
        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        return new TestableClassForDeserializer(jsonNode);
    }

    @SuppressWarnings("ClassCanBeRecord")
    @JsonDeserialize(using = DummyDeserializer.class)
    protected static class TestableClassForDeserializer {
        public final JsonNode jsonNode;

        public TestableClassForDeserializer(JsonNode jsonNode) {
            this.jsonNode = jsonNode;
        }
    }
}

package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Set;
import java.util.function.Function;

public class AbstractJsonService {
    private final ObjectMapper objectMapper;

    public AbstractJsonService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected <T> Set<T> parse(String json, Function<JsonNode, Set<T>> parseFunction) {
        try (JsonParser parser = objectMapper.createParser(json)) {
            JsonNode rootNode = parser.getCodec().readTree(parser);
            if (rootNode == null) {
                return Set.of();
            }
            return parseFunction.apply(rootNode);
        } catch (IOException e) {
            return Set.of();
        }
    }
}

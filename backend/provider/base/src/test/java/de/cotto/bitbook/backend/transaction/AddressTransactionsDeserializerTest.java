package de.cotto.bitbook.backend.transaction;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.model.TransactionHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AddressTransactionsDeserializerTest {

    private TestableAddressTransactionsDeserializer addressTransactionsDeserializer;

    @BeforeEach
    void setUp() {
        addressTransactionsDeserializer = new TestableAddressTransactionsDeserializer();
    }

    @Test
    void no_hash_in_node() throws IOException {
        try (JsonParser parser = getParserForJsonString("[false]")) {
            assertThat(addressTransactionsDeserializer.deserialize(parser, null)).isEmpty();
        }
    }

    @Test
    void limit_reached() throws IOException {
        try (JsonParser parser = getParserForJsonString("[\"x\", \"y\"]")) {
            addressTransactionsDeserializer.limit = 2;
            assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() ->
                    addressTransactionsDeserializer.deserialize(parser, null)
            );
        }
    }

    @Test
    void ok() throws IOException {
        try (JsonParser parser = getParserForJsonString("[\"x\", \"y\"]")) {
            assertThat(addressTransactionsDeserializer.deserialize(parser, null)).hasSize(2);
        }
    }

    private JsonParser getParserForJsonString(String json) throws IOException {
        return new ObjectMapper().createParser(json);
    }

    private static class TestableAddressTransactionsDeserializer
            extends AddressTransactionsDeserializer<Set<TransactionHash>> {
        private int limit = 3;

        @Override
        protected Optional<TransactionHash> getHash(JsonNode transactionReferenceNode) {
            if (transactionReferenceNode.isTextual()) {
                return Optional.of(transactionReferenceNode.asText()).map(TransactionHash::new);
            }
            return Optional.empty();
        }

        @Override
        public Set<TransactionHash> deserialize(
                JsonParser jsonParser,
                @Nullable DeserializationContext deserializationContext
        )
                throws IOException {
            return super.parseHashesWithLimit(jsonParser, limit);
        }
    }
}
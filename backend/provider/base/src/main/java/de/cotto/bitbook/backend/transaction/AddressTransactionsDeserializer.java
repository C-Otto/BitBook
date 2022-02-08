package de.cotto.bitbook.backend.transaction;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import de.cotto.bitbook.backend.model.TransactionHash;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public abstract class AddressTransactionsDeserializer<T> extends JsonDeserializer<T> {
    protected AddressTransactionsDeserializer() {
        super();
    }

    protected Set<TransactionHash> parseHashesWithLimit(JsonParser jsonParser, int limit) throws IOException {
        JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);

        Set<TransactionHash> transactionHashes = getTransactionHashes(rootNode);
        if (transactionHashes.size() >= limit) {
            throw new IllegalStateException();
        }
        return transactionHashes;
    }

    protected Set<TransactionHash> getTransactionHashes(JsonNode rootNode) {
        Set<TransactionHash> result = new LinkedHashSet<>();
        for (JsonNode transactionReferenceNode : rootNode) {
            getHash(transactionReferenceNode).ifPresent(result::add);
        }
        return result;
    }

    protected abstract Optional<TransactionHash> getHash(JsonNode transactionReferenceNode);
}

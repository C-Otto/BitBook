package de.cotto.bitbook.backend.transaction.blockstream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cotto.bitbook.backend.transaction.deserialization.AddressTransactionsDto;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

@JsonDeserialize(using = BlockstreamAddressTransactionsDto.Deserializer.class)
public class BlockstreamAddressTransactionsDto extends AddressTransactionsDto {
    protected BlockstreamAddressTransactionsDto(Set<String> transactionHashes) {
        super("", transactionHashes);
    }

    @Override
    protected void validateAddress(String expectedAddress) {
        // the blockstream.info response does not contain any address, so we cannot validate the DTO
    }

    static class Deserializer extends JsonDeserializer<BlockstreamAddressTransactionsDto> {
        private static final int MAXIMUM_WITHOUT_PAGINATION = 25;

        @Override
        public BlockstreamAddressTransactionsDto deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext
        ) throws IOException {
            JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);

            Set<String> transactionHashes = getTransactionHashes(rootNode);
            throwIfMayBePaginated(transactionHashes);
            return new BlockstreamAddressTransactionsDto(transactionHashes);
        }

        private void throwIfMayBePaginated(Set<String> transactionHashes) {
            if (transactionHashes.size() >= MAXIMUM_WITHOUT_PAGINATION) {
                throw new IllegalStateException();
            }
        }

        private Set<String> getTransactionHashes(JsonNode rootNode) {
            Set<String> result = new LinkedHashSet<>();
            for (JsonNode transactionReferenceNode : rootNode) {
                if (transactionReferenceNode.get("status").get("confirmed").booleanValue()) {
                    result.add(transactionReferenceNode.get("txid").textValue());
                }
            }
            return result;
        }
    }
}

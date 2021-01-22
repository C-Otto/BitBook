package de.cotto.bitbook.backend.transaction.sochain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cotto.bitbook.backend.transaction.deserialization.AddressTransactionsDto;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

@JsonDeserialize(using = SoChainAddressTransactionsDto.Deserializer.class)
public class SoChainAddressTransactionsDto extends AddressTransactionsDto {
    protected SoChainAddressTransactionsDto(String address, Set<String> transactionHashes) {
        super(address, transactionHashes);
    }

    static class Deserializer extends JsonDeserializer<SoChainAddressTransactionsDto> {
        @Override
        public SoChainAddressTransactionsDto deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext
        ) throws IOException {
            JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);

            int expectedNumberOfTransactions = rootNode.get("data").get("total_txs").intValue();
            Set<String> transactionHashes = getTransactionHashes(rootNode);
            if (transactionHashes.size() != expectedNumberOfTransactions) {
                throw new IllegalStateException();
            }
            return new SoChainAddressTransactionsDto(
                    getAddress(rootNode),
                    transactionHashes
            );
        }

        private String getAddress(JsonNode rootNode) {
            return rootNode.get("data").get("address").textValue();
        }

        private Set<String> getTransactionHashes(JsonNode rootNode) {
            Set<String> result = new LinkedHashSet<>();
            for (JsonNode transactionReferenceNode : getTransactionNodes(rootNode)) {
                result.add(transactionReferenceNode.get("txid").textValue());
            }
            return result;
        }

        private Iterable<JsonNode> getTransactionNodes(JsonNode rootNode) {
            return rootNode.get("data").get("txs");
        }

    }
}

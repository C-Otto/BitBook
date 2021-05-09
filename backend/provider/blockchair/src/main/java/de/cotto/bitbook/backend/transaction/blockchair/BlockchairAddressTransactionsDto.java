package de.cotto.bitbook.backend.transaction.blockchair;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import de.cotto.bitbook.backend.transaction.deserialization.AddressTransactionsDto;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

@JsonDeserialize(using = BlockchairAddressTransactionsDto.Deserializer.class)
public class BlockchairAddressTransactionsDto extends AddressTransactionsDto {
    public BlockchairAddressTransactionsDto(String address, Set<String> transactionHashes) {
        super(address, transactionHashes);
    }

    static class Deserializer extends JsonDeserializer<BlockchairAddressTransactionsDto> {
        @Override
        public BlockchairAddressTransactionsDto deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext
        ) throws IOException {
            JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);

            String address = getAddress(rootNode);
            return new BlockchairAddressTransactionsDto(address, getTransactionHashes(rootNode, address));
        }

        private String getAddress(JsonNode rootNode) {
            JsonNode dataNode = rootNode.get("data");
            return ImmutableList.copyOf(dataNode.fieldNames()).get(0);
        }

        private Set<String> getTransactionHashes(JsonNode rootNode, String address) {
            JsonNode addressNode = rootNode.get("data").get(address);
            int expectedNumberOfTransactions = addressNode.get("address").get("transaction_count").intValue();
            Set<String> result = new LinkedHashSet<>();
            for (JsonNode hashNode : addressNode.get("transactions")) {
                result.add(hashNode.textValue());
            }
            if (result.size() != expectedNumberOfTransactions) {
                throw new IllegalStateException();
            }
            return result;
        }
    }
}

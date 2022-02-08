package de.cotto.bitbook.backend.transaction.blockchair;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.deserialization.AddressTransactionsDto;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

@JsonDeserialize(using = BlockchairAddressTransactionsDto.Deserializer.class)
public class BlockchairAddressTransactionsDto extends AddressTransactionsDto {
    public BlockchairAddressTransactionsDto(Address address, Set<TransactionHash> transactionHashes) {
        super(address, transactionHashes);
    }

    static class Deserializer extends JsonDeserializer<BlockchairAddressTransactionsDto> {
        @Override
        public BlockchairAddressTransactionsDto deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext
        ) throws IOException {
            JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);

            Address address = getAddress(rootNode);
            return new BlockchairAddressTransactionsDto(address, getTransactionHashes(rootNode, address));
        }

        private Address getAddress(JsonNode rootNode) {
            JsonNode dataNode = rootNode.get("data");
            return new Address(ImmutableList.copyOf(dataNode.fieldNames()).get(0));
        }

        private Set<TransactionHash> getTransactionHashes(JsonNode rootNode, Address address) {
            JsonNode addressNode = rootNode.get("data").get(address.toString());
            int expectedNumberOfTransactions = addressNode.get("address").get("transaction_count").intValue();
            Set<TransactionHash> result = new LinkedHashSet<>();
            for (JsonNode hashNode : addressNode.get("transactions")) {
                result.add(new TransactionHash(hashNode.textValue()));
            }
            if (result.size() != expectedNumberOfTransactions) {
                throw new IllegalStateException();
            }
            return result;
        }
    }
}

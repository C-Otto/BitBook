package de.cotto.bitbook.backend.transaction.smartbit;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cotto.bitbook.backend.transaction.deserialization.AddressTransactionsDto;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

@JsonDeserialize(using = SmartbitAddressTransactionsDto.Deserializer.class)
public class SmartbitAddressTransactionsDto extends AddressTransactionsDto {
    public SmartbitAddressTransactionsDto(String address, Set<String> transactionHashes) {
        super(address, transactionHashes);
    }

    static class Deserializer extends JsonDeserializer<SmartbitAddressTransactionsDto> {
        @Override
        public SmartbitAddressTransactionsDto deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext
        ) throws IOException {
            JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
            JsonNode addressNode = rootNode.get("address");
            Set<String> hashes = getHashes(addressNode);
            int expectedNumberOfHashes = getExpectedNumberOfHashes(addressNode);
            if (hashes.size() != expectedNumberOfHashes) {
                throw new IllegalStateException();
            }
            String address = getAddress(addressNode);
            return new SmartbitAddressTransactionsDto(address, hashes);
        }

        private Set<String> getHashes(JsonNode addressNode) {
            Set<String> hashes = new LinkedHashSet<>();
            for (JsonNode transactionNode : addressNode.get("transactions")) {
                hashes.add(transactionNode.get("txid").textValue());
            }
            return hashes;
        }

        private int getExpectedNumberOfHashes(JsonNode addressNode) {
            return addressNode.get("confirmed").get("transaction_count").intValue();
        }

        private String getAddress(JsonNode addressNode) {
            return addressNode.get("address").textValue();
        }
    }
}
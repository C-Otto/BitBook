package de.cotto.bitbook.backend.transaction.btccom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cotto.bitbook.backend.transaction.deserialization.AddressTransactionsDto;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

@JsonDeserialize(using = BtcComAddressTransactionsDto.Deserializer.class)
public class BtcComAddressTransactionsDto extends AddressTransactionsDto {
    public BtcComAddressTransactionsDto(Set<String> transactionHashes) {
        super("", transactionHashes);
    }

    @Override
    protected void validateAddress(String expectedAddress) {
        // the btc.com response does not contain any address, so we cannot validate the DTO
    }

    static class Deserializer extends JsonDeserializer<BtcComAddressTransactionsDto> {
        @Override
        public BtcComAddressTransactionsDto deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext
        ) throws IOException {
            JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
            return new BtcComAddressTransactionsDto(getTransactionHashes(rootNode));
        }

        private Set<String> getTransactionHashes(JsonNode rootNode) {
            int expectedNumberOfTransactions = rootNode.get("data").get("total_count").intValue();
            Set<String> result = new LinkedHashSet<>();
            for (JsonNode transactionNode : rootNode.get("data").get("list")) {
                result.add(transactionNode.get("hash").textValue());
            }
            if (result.size() != expectedNumberOfTransactions) {
                throw new IllegalStateException();
            }
            return result;
        }
    }
}

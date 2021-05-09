package de.cotto.bitbook.backend.transaction.bitaps;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cotto.bitbook.backend.transaction.deserialization.AddressTransactionsDto;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

@JsonDeserialize(using = BitapsAddressTransactionsDto.Deserializer.class)
public class BitapsAddressTransactionsDto extends AddressTransactionsDto {
    public BitapsAddressTransactionsDto(Set<String> hashes) {
        super("", hashes);
    }

    @Override
    protected void validateAddress(String expectedAddress) {
        // the bitaps.com response does not contain any address, so we cannot validate the DTO
    }

    static class Deserializer extends JsonDeserializer<BitapsAddressTransactionsDto> {
        @Override
        public BitapsAddressTransactionsDto deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext
        ) throws IOException {
            JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);
            JsonNode detailsNode = rootNode.get("data");
            throwIfPaginated(detailsNode);
            return new BitapsAddressTransactionsDto(getHashes(detailsNode));
        }

        private void throwIfPaginated(JsonNode detailsNode) {
            int onePage = 1;
            if (detailsNode.get("pages").intValue() != onePage) {
                throw new IllegalStateException();
            }
        }

        private Set<String> getHashes(JsonNode detailsNode) {
            Set<String> hashes = new LinkedHashSet<>();
            for (JsonNode transactionNode : detailsNode.get("list")) {
                hashes.add(transactionNode.get("txId").textValue());
            }
            return hashes;
        }
    }
}

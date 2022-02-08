package de.cotto.bitbook.backend.transaction.mempoolspace;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.transaction.AddressTransactionsDeserializer;
import de.cotto.bitbook.backend.transaction.deserialization.AddressTransactionsDto;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@JsonDeserialize(using = MempoolSpaceAddressTransactionsDto.Deserializer.class)
public class MempoolSpaceAddressTransactionsDto extends AddressTransactionsDto {
    protected MempoolSpaceAddressTransactionsDto(Set<String> transactionHashes) {
        super(Address.NONE, transactionHashes);
    }

    @Override
    protected void validateAddress(Address expectedAddress) {
        // the mempool.space response does not contain any address, so we cannot validate the DTO
    }

    static class Deserializer extends AddressTransactionsDeserializer<MempoolSpaceAddressTransactionsDto> {
        private static final int MAXIMUM_WITHOUT_PAGINATION = 25;

        @Override
        public MempoolSpaceAddressTransactionsDto deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext
        ) throws IOException {
            Set<String> transactionHashes = parseHashesWithLimit(jsonParser, MAXIMUM_WITHOUT_PAGINATION);
            return new MempoolSpaceAddressTransactionsDto(transactionHashes);
        }

        @Override
        protected Optional<String> getHash(JsonNode transactionReferenceNode) {
            return Optional.of(transactionReferenceNode.get("txid").textValue());
        }
    }
}

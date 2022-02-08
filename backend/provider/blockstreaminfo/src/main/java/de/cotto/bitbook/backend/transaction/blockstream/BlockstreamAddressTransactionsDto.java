package de.cotto.bitbook.backend.transaction.blockstream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.AddressTransactionsDeserializer;
import de.cotto.bitbook.backend.transaction.deserialization.AddressTransactionsDto;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@JsonDeserialize(using = BlockstreamAddressTransactionsDto.Deserializer.class)
public class BlockstreamAddressTransactionsDto extends AddressTransactionsDto {
    protected BlockstreamAddressTransactionsDto(Set<TransactionHash> transactionHashes) {
        super(Address.NONE, transactionHashes);
    }

    @Override
    protected void validateAddress(Address expectedAddress) {
        // the blockstream.info response does not contain any address, so we cannot validate the DTO
    }

    static class Deserializer extends AddressTransactionsDeserializer<BlockstreamAddressTransactionsDto> {
        private static final int MAXIMUM_WITHOUT_PAGINATION = 25;

        @Override
        public BlockstreamAddressTransactionsDto deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext
        ) throws IOException {
            Set<TransactionHash> transactionHashes = parseHashesWithLimit(jsonParser, MAXIMUM_WITHOUT_PAGINATION);
            return new BlockstreamAddressTransactionsDto(transactionHashes);
        }

        @Override
        protected Optional<TransactionHash> getHash(JsonNode transactionReferenceNode) {
            if (transactionReferenceNode.get("status").get("confirmed").booleanValue()) {
                return Optional.of(transactionReferenceNode.get("txid").textValue()).map(TransactionHash::new);
            }
            return Optional.empty();
        }
    }
}

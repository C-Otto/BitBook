package de.cotto.bitbook.backend.transaction.blockcypher;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cotto.bitbook.backend.transaction.deserialization.AddressTransactionsDto;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@JsonDeserialize(using = BlockcypherAddressTransactionsDto.Deserializer.class)
public class BlockcypherAddressTransactionsDto extends AddressTransactionsDto {
    private final boolean incomplete;
    private final int lowestCompletedBlockHeight;

    public BlockcypherAddressTransactionsDto(
            String address,
            Set<String> transactionHashes,
            boolean incomplete,
            int lowestCompletedBlockHeight
    ) {
        super(address, transactionHashes);
        this.incomplete = incomplete;
        this.lowestCompletedBlockHeight = lowestCompletedBlockHeight;
    }

    public boolean isIncomplete() {
        return incomplete;
    }

    public int getLowestCompletedBlockHeight() {
        return lowestCompletedBlockHeight;
    }

    public BlockcypherAddressTransactionsDto combine(BlockcypherAddressTransactionsDto other) {
        Set<String> combinedTransactionHashes = new LinkedHashSet<>();
        combinedTransactionHashes.addAll(requireNonNull(getTransactionHashes()));
        combinedTransactionHashes.addAll(requireNonNull(other.getTransactionHashes()));
        return new BlockcypherAddressTransactionsDto(
                getAddress(),
                combinedTransactionHashes,
                incomplete,
                lowestCompletedBlockHeight
        );
    }

    static class Deserializer extends JsonDeserializer<BlockcypherAddressTransactionsDto> {
        @Override
        public BlockcypherAddressTransactionsDto deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext
        ) throws IOException {
            JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);

            boolean hasMore = getHasMore(rootNode);
            int lowestCompletedBlockHeight = getLowestCompletedBlockHeight(rootNode);
            return new BlockcypherAddressTransactionsDto(
                    getAddress(rootNode),
                    getTransactionHashes(rootNode),
                    hasMore,
                    lowestCompletedBlockHeight
            );
        }

        private int getLowestCompletedBlockHeight(JsonNode rootNode) {
            return getBlockHeights(rootNode)
                    .stream().mapToInt(i -> i)
                    .min()
                    .orElse(Integer.MAX_VALUE);
        }

        private String getAddress(JsonNode rootNode) {
            return rootNode.get("address").textValue();
        }

        private Set<String> getTransactionHashes(JsonNode rootNode) {
            Set<String> result = new LinkedHashSet<>();
            for (JsonNode transactionReferenceNode : getTransactionNodes(rootNode)) {
                long value = transactionReferenceNode.get("value").longValue();
                if (value > 0) {
                    result.add(transactionReferenceNode.get("tx_hash").textValue());
                }
            }
            return result;
        }

        private Set<Integer> getBlockHeights(JsonNode rootNode) {
            Set<Integer> result = new LinkedHashSet<>();
            for (JsonNode blockHeights : getTransactionNodes(rootNode)) {
                result.add(blockHeights.get("block_height").intValue());
            }
            return result;
        }

        private boolean getHasMore(JsonNode rootNode) {
            JsonNode node = rootNode.get("hasMore");
            if (node == null) {
                return false;
            }
            return node.booleanValue();
        }

        private Iterable<JsonNode> getTransactionNodes(JsonNode rootNode) {
            JsonNode nodes = rootNode.get("txrefs");
            if (nodes == null) {
                return Collections.emptySet();
            }
            return nodes;
        }

    }
}

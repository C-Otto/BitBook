package de.cotto.bitbook.backend.transaction.electrs;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.HexString;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.electrs.jsonrpc.JsonRpcClient;
import de.cotto.bitbook.backend.transaction.electrs.jsonrpc.JsonRpcMessage;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class ElectrsClient {
    private static final String HOST = "localhost";
    private static final int PORT = 50_001;

    private static final String SUBSCRIBE_METHOD = "blockchain.scripthash.subscribe";
    private static final String GET_HISTORY_METHOD = "blockchain.scripthash.get_history";

    private final JsonRpcClient jsonRpcClient;

    public ElectrsClient() {
        this(new JsonRpcClient(HOST, PORT));
    }

    @VisibleForTesting
    ElectrsClient(JsonRpcClient jsonRpcClient) {
        this.jsonRpcClient = jsonRpcClient;
    }

    public Optional<Set<TransactionHash>> getTransactionHashes(Address address) {
        HexString scriptHash = getReversedScriptHash(address);
        JsonRpcMessage subscribeMessage = new JsonRpcMessage(SUBSCRIBE_METHOD, scriptHash);
        JsonRpcMessage getHistoryMessage = new JsonRpcMessage(GET_HISTORY_METHOD, scriptHash);
        jsonRpcClient.sendMessages(subscribeMessage, getHistoryMessage);

        return getFutureResult(getHistoryMessage.getFuture());
    }

    private HexString getReversedScriptHash(Address address) {
        HexString script = address.getScript();
        HexString scriptHash = script.getSha256Hash();
        byte[] byteArray = scriptHash.getByteArray();
        ArrayUtils.reverse(byteArray);
        return new HexString(byteArray);
    }

    private Optional<Set<TransactionHash>> getFutureResult(CompletableFuture<JsonNode> future) {
        try {
            Set<TransactionHash> hashes = future
                    .thenApply(this::parse)
                    .get(1, TimeUnit.SECONDS);
            return Optional.of(hashes);
        } catch (CompletionException | InterruptedException | ExecutionException | TimeoutException exception) {
            return Optional.empty();
        }
    }

    private Set<TransactionHash> parse(JsonNode jsonNode) {
        Set<TransactionHash> result = new LinkedHashSet<>();
        JsonNode transactions = jsonNode.get("result");
        for (JsonNode tx : transactions) {
            if (tx.get("height").intValue() <= 0) {
                continue;
            }
            result.add(new TransactionHash(tx.get("tx_hash").textValue()));
        }
        return result;
    }
}

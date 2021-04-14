package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import de.cotto.bitbook.lnd.features.ClosedChannelsService;
import de.cotto.bitbook.lnd.features.SweepTransactionsService;
import de.cotto.bitbook.lnd.features.UnspentOutputsService;
import de.cotto.bitbook.lnd.model.CloseType;
import de.cotto.bitbook.lnd.model.ClosedChannel;
import de.cotto.bitbook.lnd.model.Initiator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

@Component
public class LndService {
    private static final String UNKNOWN_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    private final ObjectMapper objectMapper;
    private final ClosedChannelsService closedChannelsService;
    private final UnspentOutputsService unspentOutputsService;
    private final SweepTransactionsService sweepTransactionsService;
    private final TransactionService transactionService;

    public LndService(
            ObjectMapper objectMapper,
            ClosedChannelsService closedChannelsService,
            UnspentOutputsService unspentOutputsService,
            SweepTransactionsService sweepTransactionsService,
            TransactionService transactionService
    ) {
        this.objectMapper = objectMapper;
        this.closedChannelsService = closedChannelsService;
        this.unspentOutputsService = unspentOutputsService;
        this.sweepTransactionsService = sweepTransactionsService;
        this.transactionService = transactionService;
    }

    public long addFromSweeps(String json) {
        Set<String> hashes = parse(json, this::parseSweepTransactionHashes);
        return sweepTransactionsService.addFromSweeps(hashes);
    }

    public long addFromUnspentOutputs(String json) {
        Set<String> addresses = parse(json, this::parseAddressesFromUnspentOutputs);
        return unspentOutputsService.addFromUnspentOutputs(addresses);
    }

    public long addFromClosedChannels(String json) {
        Set<ClosedChannel> closedChannels = parse(json, this::parseClosedChannels);
        return closedChannelsService.addFromClosedChannels(closedChannels);
    }

    private <T> Set<T> parse(String json, Function<JsonNode, Set<T>> parseFunction) {
        try (JsonParser parser = objectMapper.createParser(json)) {
            JsonNode rootNode = parser.getCodec().readTree(parser);
            if (rootNode == null) {
                return Set.of();
            }
            return parseFunction.apply(rootNode);
        } catch (IOException e) {
            return Set.of();
        }
    }

    private Set<String> parseSweepTransactionHashes(JsonNode rootNode) {
        JsonNode sweeps = rootNode.get("Sweeps");
        if (sweeps == null) {
            return Set.of();
        }
        JsonNode transactionIds = sweeps.get("TransactionIds");
        if (transactionIds == null) {
            return Set.of();
        }
        JsonNode hashesArray = transactionIds.get("transaction_ids");
        if (hashesArray == null) {
            return Set.of();
        }
        Set<String> hashes = new LinkedHashSet<>();
        for (JsonNode hash : hashesArray) {
            hashes.add(hash.textValue());
        }
        return hashes;
    }

    private Set<String> parseAddressesFromUnspentOutputs(JsonNode rootNode) {
        JsonNode utxos = rootNode.get("utxos");
        if (utxos == null) {
            return Set.of();
        }
        Set<String> addresses = new LinkedHashSet<>();
        for (JsonNode utxo : utxos) {
            if (utxo.get("confirmations").intValue() == 0) {
                continue;
            }
            addresses.add(utxo.get("address").textValue());
        }
        return addresses;
    }

    public Set<ClosedChannel> parseClosedChannels(JsonNode jsonNode) {
        JsonNode channels = jsonNode.get("channels");
        if (channels == null || !channels.isArray()) {
            return Set.of();
        }
        preloadTransactionHashes(channels);
        Set<ClosedChannel> result = new LinkedHashSet<>();
        for (JsonNode channelNode : channels) {
            if (getValidTransactionHashes(channelNode).isEmpty()) {
                continue;
            }
            result.add(parseClosedChannel(channelNode));
        }
        return result;
    }

    private void preloadTransactionHashes(JsonNode channels) {
        Set<String> allTransactionHashes = new LinkedHashSet<>();
        for (JsonNode channelNode : channels) {
            allTransactionHashes.addAll(getValidTransactionHashes(channelNode));
        }
        allTransactionHashes.parallelStream().forEach(transactionService::getTransactionDetails);
    }

    private Set<String> getValidTransactionHashes(JsonNode channelNode) {
        int closeHeight = channelNode.get("close_height").intValue();
        if (closeHeight == 0) {
            return Set.of();
        }
        Set<String> hashes = new LinkedHashSet<>();
        hashes.add(channelNode.get("closing_tx_hash").textValue());
        hashes.add(parseOpeningTransaction(channelNode));
        if (hashes.contains(UNKNOWN_HASH)) {
            return Set.of();
        }
        return hashes;
    }

    private ClosedChannel parseClosedChannel(JsonNode channelNode) {
        String openingTransactionHash = parseOpeningTransaction(channelNode);
        String closingTransactionHash = channelNode.get("closing_tx_hash").textValue();
        Transaction openingTransaction = transactionService.getTransactionDetails(openingTransactionHash);
        Transaction closingTransaction = transactionService.getTransactionDetails(closingTransactionHash);
        return ClosedChannel.builder()
                .withChainHash(channelNode.get("chain_hash").textValue())
                .withOpeningTransaction(openingTransaction)
                .withClosingTransaction(closingTransaction)
                .withRemotePubkey(channelNode.get("remote_pubkey").textValue())
                .withSettledBalance(Coins.ofSatoshis(Long.parseLong(channelNode.get("settled_balance").textValue())))
                .withOpenInitiator(Initiator.fromString(channelNode.get("open_initiator").textValue()))
                .withCloseType(CloseType.fromStringAndInitiator(
                        channelNode.get("close_type").textValue(),
                        channelNode.get("close_initiator").textValue()
                ))
                .build();
    }

    private String parseOpeningTransaction(JsonNode channel) {
        String channelPoint = channel.get("channel_point").textValue();
        return channelPoint.substring(0, channelPoint.indexOf(':'));
    }
}

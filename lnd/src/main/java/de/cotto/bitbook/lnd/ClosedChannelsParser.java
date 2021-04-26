package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.databind.JsonNode;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import de.cotto.bitbook.lnd.model.CloseType;
import de.cotto.bitbook.lnd.model.ClosedChannel;
import de.cotto.bitbook.lnd.model.Initiator;
import de.cotto.bitbook.lnd.model.Resolution;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class ClosedChannelsParser {
    private static final String UNKNOWN_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    private final TransactionService transactionService;

    public ClosedChannelsParser(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public Set<ClosedChannel> parse(JsonNode jsonNode) {
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
        transactionService.getTransactionDetails(allTransactionHashes);
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
                .withResolutions(getResolutions(channelNode))
                .build();
    }

    private Set<Resolution> getResolutions(JsonNode channelNode) {
        Set<Resolution> result = new LinkedHashSet<>();
        for (JsonNode resolutionNode : channelNode.get("resolutions")) {
            String sweepTransactionHash = resolutionNode.get("sweep_txid").textValue();
            result.add(new Resolution(sweepTransactionHash));
        }
        return result;
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

    private String parseOpeningTransaction(JsonNode channel) {
        String channelPoint = channel.get("channel_point").textValue();
        return channelPoint.substring(0, channelPoint.indexOf(':'));
    }
}

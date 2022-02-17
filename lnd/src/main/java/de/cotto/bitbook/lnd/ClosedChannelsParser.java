package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.databind.JsonNode;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.lnd.model.CloseType;
import de.cotto.bitbook.lnd.model.ClosedChannel;
import de.cotto.bitbook.lnd.model.Initiator;
import de.cotto.bitbook.lnd.model.Resolution;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

import static de.cotto.bitbook.backend.model.Chain.BTC;

@Component
public class ClosedChannelsParser {
    private static final TransactionHash UNKNOWN_HASH =
            new TransactionHash("0000000000000000000000000000000000000000000000000000000000000000");

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
        Set<TransactionHash> allTransactionHashes = new LinkedHashSet<>();
        for (JsonNode channelNode : channels) {
            allTransactionHashes.addAll(getValidTransactionHashes(channelNode));
        }
        transactionService.getTransactionDetails(allTransactionHashes, BTC);
    }

    private ClosedChannel parseClosedChannel(JsonNode channelNode) {
        TransactionHash openingTransactionHash = parseOpeningTransaction(channelNode);
        TransactionHash closingTransactionHash = new TransactionHash(channelNode.get("closing_tx_hash").textValue());
        Transaction openingTransaction = transactionService.getTransactionDetails(openingTransactionHash, BTC);
        Transaction closingTransaction = transactionService.getTransactionDetails(closingTransactionHash, BTC);
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

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<Resolution> getResolutions(JsonNode channelNode) {
        Set<Resolution> result = new LinkedHashSet<>();
        for (JsonNode resolutionNode : channelNode.get("resolutions")) {
            TransactionHash sweepTransactionHash = new TransactionHash(resolutionNode.get("sweep_txid").textValue());
            String resolutionType = resolutionNode.get("resolution_type").textValue();
            String outcome = resolutionNode.get("outcome").textValue();
            result.add(new Resolution(sweepTransactionHash, resolutionType, outcome));
        }
        return result;
    }

    private Set<TransactionHash> getValidTransactionHashes(JsonNode channelNode) {
        int closeHeight = channelNode.get("close_height").intValue();
        if (closeHeight == 0) {
            return Set.of();
        }
        Set<TransactionHash> hashes = new LinkedHashSet<>();
        hashes.add(new TransactionHash(channelNode.get("closing_tx_hash").textValue()));
        hashes.add(parseOpeningTransaction(channelNode));
        if (hashes.contains(UNKNOWN_HASH)) {
            return Set.of();
        }
        return hashes;
    }

    private TransactionHash parseOpeningTransaction(JsonNode channel) {
        String channelPoint = channel.get("channel_point").textValue();
        return ChannelPointParser.getTransactionHash(channelPoint);
    }
}

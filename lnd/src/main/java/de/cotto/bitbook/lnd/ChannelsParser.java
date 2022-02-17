package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.databind.JsonNode;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.lnd.model.Channel;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static de.cotto.bitbook.backend.model.Chain.BTC;

@Component
public class ChannelsParser {
    private final TransactionService transactionService;

    public ChannelsParser(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public Set<Channel> parse(JsonNode jsonNode) {
        JsonNode channels = jsonNode.get("channels");
        if (channels == null || !channels.isArray()) {
            return Set.of();
        }
        preloadTransactionDetails(channels);

        Set<Channel> result = new LinkedHashSet<>();
        for (JsonNode channelNode : channels) {
            parseChannel(channelNode).ifPresent(result::add);
        }
        return result;
    }

    private void preloadTransactionDetails(JsonNode channels) {
        Set<TransactionHash> hashes = new LinkedHashSet<>();
        for (JsonNode channelNode : channels) {
            hashes.add(getOpeningTransactionHash(channelNode));
        }
        transactionService.getTransactionDetails(hashes, BTC);
    }

    private Optional<Channel> parseChannel(JsonNode channelNode) {
        Transaction transaction = transactionService.getTransactionDetails(getOpeningTransactionHash(channelNode), BTC);
        if (transaction.isInvalid()) {
            return Optional.empty();
        }
        return Optional.of(new Channel(
                channelNode.get("initiator").booleanValue(),
                channelNode.get("remote_pubkey").textValue(),
                transaction,
                getOutputIndex(channelNode)));
    }

    private TransactionHash getOpeningTransactionHash(JsonNode channelNode) {
        String channelPoint = channelNode.get("channel_point").textValue();
        return ChannelPointParser.getTransactionHash(channelPoint);
    }

    private int getOutputIndex(JsonNode channelNode) {
        String channelPoint = channelNode.get("channel_point").textValue();
        return ChannelPointParser.getOutputIndex(channelPoint);
    }
}

package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.databind.JsonNode;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.lnd.model.Channel;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

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
            result.add(parseChannel(channelNode));
        }
        return result;
    }

    private void preloadTransactionDetails(JsonNode channels) {
        Set<String> hashes = new LinkedHashSet<>();
        for (JsonNode channelNode : channels) {
            hashes.add(getOpeningTransactionHash(channelNode));
        }
        transactionService.getTransactionDetails(hashes);
    }

    private Channel parseChannel(JsonNode channelNode) {
        return new Channel(
                channelNode.get("initiator").booleanValue(),
                channelNode.get("remote_pubkey").textValue(),
                transactionService.getTransactionDetails(getOpeningTransactionHash(channelNode)),
                getOutputIndex(channelNode));
    }

    private String getOpeningTransactionHash(JsonNode channelNode) {
        String channelPoint = channelNode.get("channel_point").textValue();
        return channelPoint.substring(0, channelPoint.indexOf(':'));
    }

    private int getOutputIndex(JsonNode channelNode) {
        String channelPoint = channelNode.get("channel_point").textValue();
        return Integer.parseInt(channelPoint.substring(channelPoint.indexOf(':') + 1));
    }
}

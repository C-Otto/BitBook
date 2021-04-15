package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.lnd.features.ChannelsService;
import de.cotto.bitbook.lnd.features.ClosedChannelsService;
import de.cotto.bitbook.lnd.features.SweepTransactionsService;
import de.cotto.bitbook.lnd.features.UnspentOutputsService;
import de.cotto.bitbook.lnd.model.Channel;
import de.cotto.bitbook.lnd.model.ClosedChannel;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

@Component
public class LndService {
    private final ObjectMapper objectMapper;
    private final ClosedChannelsService closedChannelsService;
    private final UnspentOutputsService unspentOutputsService;
    private final SweepTransactionsService sweepTransactionsService;
    private final ClosedChannelsParser closedChannelsParser;
    private final ChannelsService channelsService;
    private final ChannelsParser channelsParser;

    public LndService(
            ObjectMapper objectMapper,
            ClosedChannelsService closedChannelsService,
            UnspentOutputsService unspentOutputsService,
            SweepTransactionsService sweepTransactionsService,
            ClosedChannelsParser closedChannelsParser,
            ChannelsService channelsService, ChannelsParser channelsParser) {
        this.objectMapper = objectMapper;
        this.closedChannelsService = closedChannelsService;
        this.unspentOutputsService = unspentOutputsService;
        this.sweepTransactionsService = sweepTransactionsService;
        this.closedChannelsParser = closedChannelsParser;
        this.channelsService = channelsService;
        this.channelsParser = channelsParser;
    }

    public long addFromSweeps(String json) {
        Set<String> hashes = parse(json, this::parseSweepTransactionHashes);
        return sweepTransactionsService.addFromSweeps(hashes);
    }

    public long addFromUnspentOutputs(String json) {
        Set<String> addresses = parse(json, this::parseAddressesFromUnspentOutputs);
        return unspentOutputsService.addFromUnspentOutputs(addresses);
    }

    public long addFromChannels(String json) {
        Set<Channel> channels = parse(json, channelsParser::parse);
        return channelsService.addFromChannels(channels);
    }

    public long addFromClosedChannels(String json) {
        Set<ClosedChannel> closedChannels = parse(json, closedChannelsParser::parse);
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
}

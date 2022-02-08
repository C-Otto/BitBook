package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.lnd.features.ChannelsService;
import de.cotto.bitbook.lnd.features.ClosedChannelsService;
import de.cotto.bitbook.lnd.features.OnchainTransactionsService;
import de.cotto.bitbook.lnd.features.SweepTransactionsService;
import de.cotto.bitbook.lnd.features.UnspentOutputsService;
import de.cotto.bitbook.lnd.model.Channel;
import de.cotto.bitbook.lnd.model.ClosedChannel;
import de.cotto.bitbook.lnd.model.OnchainTransaction;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class LndService extends AbstractJsonService {
    private final ClosedChannelsService closedChannelsService;
    private final UnspentOutputsService unspentOutputsService;
    private final SweepTransactionsService sweepTransactionsService;
    private final ClosedChannelsParser closedChannelsParser;
    private final ChannelsService channelsService;
    private final ChannelsParser channelsParser;
    private final OnchainTransactionsParser onchainTransactionsParser;
    private final OnchainTransactionsService onchainTransactionsService;

    public LndService(
            ObjectMapper objectMapper,
            ClosedChannelsService closedChannelsService,
            UnspentOutputsService unspentOutputsService,
            SweepTransactionsService sweepTransactionsService,
            ClosedChannelsParser closedChannelsParser,
            ChannelsService channelsService,
            ChannelsParser channelsParser,
            OnchainTransactionsParser onchainTransactionsParser,
            OnchainTransactionsService onchainTransactionsService
    ) {
        super(objectMapper);
        this.closedChannelsService = closedChannelsService;
        this.unspentOutputsService = unspentOutputsService;
        this.sweepTransactionsService = sweepTransactionsService;
        this.closedChannelsParser = closedChannelsParser;
        this.channelsService = channelsService;
        this.channelsParser = channelsParser;
        this.onchainTransactionsParser = onchainTransactionsParser;
        this.onchainTransactionsService = onchainTransactionsService;
    }

    public long addFromSweeps(String json) {
        Set<String> hashes = parse(json, this::parseSweepTransactionHashes);
        return sweepTransactionsService.addFromSweeps(hashes);
    }

    public long addFromUnspentOutputs(String json) {
        Set<Address> addresses = parse(json, this::parseAddressesFromUnspentOutputs);
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

    public long addFromOnchainTransactions(String json) {
        Set<OnchainTransaction> onchainTransactions = parse(json, onchainTransactionsParser::parse);
        return onchainTransactionsService.addFromOnchainTransactions(onchainTransactions);
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

    private Set<Address> parseAddressesFromUnspentOutputs(JsonNode rootNode) {
        JsonNode utxos = rootNode.get("utxos");
        if (utxos == null) {
            return Set.of();
        }
        Set<Address> addresses = new LinkedHashSet<>();
        for (JsonNode utxo : utxos) {
            if (utxo.get("confirmations").intValue() == 0) {
                continue;
            }
            addresses.add(new Address(utxo.get("address").textValue()));
        }
        return addresses;
    }
}

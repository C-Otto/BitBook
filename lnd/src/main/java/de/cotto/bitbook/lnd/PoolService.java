package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.lnd.features.PoolLeasesService;
import de.cotto.bitbook.lnd.model.PoolLease;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class PoolService extends AbstractJsonService {
    private final PoolLeasesService poolLeasesService;

    public PoolService(
            ObjectMapper objectMapper,
            PoolLeasesService poolLeasesService) {
        super(objectMapper);
        this.poolLeasesService = poolLeasesService;
    }

    public long addFromLeases(String json) {
        Set<PoolLease> leases = parse(json, this::parseLeases);
        return poolLeasesService.addFromLeases(leases);
    }

    private Set<PoolLease> parseLeases(JsonNode jsonNode) {
        JsonNode leaseNodes = jsonNode.get("leases");
        if (leaseNodes == null || !leaseNodes.isArray()) {
            return Set.of();
        }
        Set<PoolLease> result = new LinkedHashSet<>();
        for (JsonNode leaseNode : leaseNodes) {
            boolean purchased = leaseNode.get("purchased").booleanValue();
            if (purchased) {
                continue;
            }
            String channelPoint = leaseNode.get("channel_point").textValue();
            String transactionHash = ChannelPointParser.getTransactionHash(channelPoint);
            int outputIndex = ChannelPointParser.getOutputIndex(channelPoint);
            String pubKey = leaseNode.get("channel_node_key").textValue();
            Coins premium = Coins.ofSatoshis(leaseNode.get("premium_sat").longValue());
            Coins executionFee = Coins.ofSatoshis(leaseNode.get("execution_fee_sat").longValue());
            Coins chainFee = Coins.ofSatoshis(leaseNode.get("chain_fee_sat").longValue());
            result.add(new PoolLease(transactionHash, outputIndex, pubKey, premium, executionFee, chainFee));
        }
        return result;
    }
}

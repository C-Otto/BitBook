package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.databind.JsonNode;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.lnd.model.OnchainTransaction;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class OnchainTransactionsParser {
    public OnchainTransactionsParser() {
        // default constructor
    }

    public Set<OnchainTransaction> parse(JsonNode jsonNode) {
        JsonNode transactions = jsonNode.get("transactions");
        if (transactions == null || !transactions.isArray()) {
            return Set.of();
        }
        Set<OnchainTransaction> result = new LinkedHashSet<>();
        for (JsonNode transactionNode : transactions) {
            String transactionHash = transactionNode.get("tx_hash").textValue();
            String label = transactionNode.get("label").textValue();
            Coins amount = Coins.ofSatoshis(Long.parseLong(transactionNode.get("amount").textValue()));
            Coins fees = Coins.ofSatoshis(Long.parseLong(transactionNode.get("total_fees").textValue()));
            result.add(new OnchainTransaction(transactionHash, label, amount, fees));
        }
        return result;
    }
}

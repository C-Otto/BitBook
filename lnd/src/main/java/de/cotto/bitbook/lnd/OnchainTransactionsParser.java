package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.databind.JsonNode;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.lnd.model.OnchainTransaction;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class OnchainTransactionsParser {
    public OnchainTransactionsParser() {
        // default constructor
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Set<OnchainTransaction> parse(JsonNode jsonNode) {
        JsonNode transactions = jsonNode.get("transactions");
        if (transactions == null || !transactions.isArray()) {
            return Set.of();
        }
        Set<OnchainTransaction> result = new LinkedHashSet<>();
        for (JsonNode transactionNode : transactions) {
            TransactionHash transactionHash = new TransactionHash(transactionNode.get("tx_hash").textValue());
            String label = transactionNode.get("label").textValue();
            Coins amount = Coins.ofSatoshis(Long.parseLong(transactionNode.get("amount").textValue()));
            Coins fees = Coins.ofSatoshis(Long.parseLong(transactionNode.get("total_fees").textValue()));
            Set<Address> ownedAddresses = getOwnedAddressesForTransaction(transactionNode);
            result.add(new OnchainTransaction(transactionHash, label, amount, fees, ownedAddresses));
        }
        return result;
    }

    private Set<Address> getOwnedAddressesForTransaction(JsonNode transactionNode) {
        Set<Address> result = new LinkedHashSet<>();
        if (transactionNode.has("output_details")) {
            for (JsonNode node : transactionNode.get("output_details")) {
                if (node.has("is_our_address") && node.get("is_our_address").booleanValue() && node.has("address")) {
                    String addressString = node.get("address").textValue();
                    result.add(new Address(addressString));
                }
            }
        }

        return result;
    }
}

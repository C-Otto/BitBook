package de.cotto.bitbook.graphviz;

import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.transaction.AddressTransactionsService;
import de.cotto.bitbook.backend.transaction.BalanceService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

@Component
public class GraphvizService {
    private final AddressOwnershipService addressOwnershipService;
    private final AddressTransactionsService addressTransactionsService;
    private final TransactionService transactionService;
    private final BalanceService balanceService;

    private static final String FILENAME = "dot.txt";

    public GraphvizService(
            AddressOwnershipService addressOwnershipService,
            AddressTransactionsService addressTransactionsService,
            TransactionService transactionService,
            BalanceService balanceService
    ) {
        this.addressOwnershipService = addressOwnershipService;
        this.addressTransactionsService = addressTransactionsService;
        this.transactionService = transactionService;
        this.balanceService = balanceService;
    }

    public void createDottyFile() {
        Set<AddressWithDescription> ownedAddressesWithDescription = addressOwnershipService.getOwnedAddressesWithDescription();
        Set<String> ownedAddresses = ownedAddressesWithDescription.stream()
                .map(AddressWithDescription::getAddress)
                .collect(toSet());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getPreamble());
        appendClusters(stringBuilder, ownedAddressesWithDescription);
        appendTransactions(stringBuilder, ownedAddresses);
        stringBuilder.append(getEpilog());
        String string = stringBuilder.toString();

        writeToFile(string);
    }

    private void appendClusters(StringBuilder stringBuilder, Set<AddressWithDescription> ownedAddressesWithDescription) {
        Map<String, Set<String>> byDescription = ownedAddressesWithDescription.stream().collect(
                groupingBy(AddressWithDescription::getDescription, mapping(AddressWithDescription::getAddress, toSet()))
        );
        for (Map.Entry<String, Set<String>> entry : byDescription.entrySet()) {
            String description = entry.getKey();
            String clusterName = toGraphvizKey(description, "cluster_");
            stringBuilder.append("\tsubgraph ");
            stringBuilder.append(clusterName);
            stringBuilder.append(" {\n");
            stringBuilder.append("\t\tstyle = filled;\n");
            stringBuilder.append("\t\tcolor = lightgrey;\n");
            stringBuilder.append("\t\tnode [shape=plaintext,style=filled,color=white];\n");
            stringBuilder.append("\t\tlabel = \"");
            stringBuilder.append(description);
            stringBuilder.append("\";\n");

            for (String address : entry.getValue()) {
                stringBuilder.append("\t\t")
                    .append(toGraphvizKey(address, "address_"))
                    .append(" [");
                Coins balance = balanceService.getBalance(address);
                if (!balance.equals(Coins.NONE)) {
                    stringBuilder.append("fillcolor=\"#ff0000\",");
                }
                stringBuilder.append("label=\"")
                    .append(address, 0, 5)
                    .append("…\"];\n");
            }
            stringBuilder.append("\t}\n");
        }
    }

    private void appendTransactions(StringBuilder stringBuilder, Set<String> ownedAddresses) {
        ownedAddresses.stream()
                .map(addressTransactionsService::getTransactions)
                .map(AddressTransactions::getTransactionHashes)
                .flatMap(Set::stream)
                .map(transactionService::getTransactionDetails)
                .distinct()
                .forEach(transaction -> {
                    for (String inputAddress : transaction.getInputAddresses()) {
                        if (!ownedAddresses.contains(inputAddress)) {
                            continue;
                        }
                        for (String outputAddress : transaction.getOutputAddresses()) {
                            if (!ownedAddresses.contains(outputAddress)) {
                                continue;
                            }
                            String sourceAddress = toGraphvizKey(inputAddress, "address_");
                            String targetAddress = toGraphvizKey(outputAddress, "address_");
                            stringBuilder.append("\t")
                                    .append(sourceAddress)
                                    .append(" -> ")
                                    .append(targetAddress)
                                    .append(";\n");
                        }
                    }
        });

    }

    private String toGraphvizKey(String string, String prefix) {
        return prefix + DigestUtils.sha256Hex(string);
    }

    private String getPreamble() {
        return "digraph G {\n";
    }

    private String getEpilog() {
        return "}\n";
    }

    private void writeToFile(String string) {
        Path path = Paths.get(FILENAME);
        try {
            Files.writeString(path, string);
        } catch (IOException e) {
            // ignore
        }
    }
}

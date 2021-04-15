package de.cotto.bitbook.lnd;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import de.cotto.bitbook.ownership.AddressOwnershipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Component
public class LndService {
    private static final String DEFAULT_ADDRESS_DESCRIPTION = "lnd";
    private static final String SWEEP_TRANSACTION_DESCRIPTION = "lnd sweep transaction";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper;
    private final TransactionService transactionService;
    private final TransactionDescriptionService transactionDescriptionService;
    private final AddressDescriptionService addressDescriptionService;
    private final AddressOwnershipService addressOwnershipService;

    public LndService(
            TransactionService transactionService,
            AddressDescriptionService addressDescriptionService,
            TransactionDescriptionService transactionDescriptionService,
            AddressOwnershipService addressOwnershipService,
            ObjectMapper objectMapper
    ) {
        this.transactionService = transactionService;
        this.addressDescriptionService = addressDescriptionService;
        this.transactionDescriptionService = transactionDescriptionService;
        this.addressOwnershipService = addressOwnershipService;
        this.objectMapper = objectMapper;
    }

    public long lndAddFromSweeps(String json) {
        Set<String> hashes = parse(json, this::parseHashes).orElse(Set.of());
        return hashes.stream()
                .map(this::getGetTransactionDetails)
                .filter(this::isSweepTransaction)
                .map(this::addTransactionDescription)
                .map(this::addAddressDescriptions)
                .map(this::setAddressesAsOwned)
                .count();
    }

    public long lndAddUnspentOutputs(String json) {
        Set<String> addresses = parse(json, this::parseAddressesFromUnspentOutputs).orElse(Set.of());
        addresses.forEach(addressOwnershipService::setAddressAsOwned);
        addresses.forEach(address -> addressDescriptionService.set(address, DEFAULT_ADDRESS_DESCRIPTION));
        return addresses.size();
    }

    private <T> Optional<T> parse(String json, Function<JsonNode, T> parseFunction) {
        try (JsonParser parser = objectMapper.createParser(json)) {
            JsonNode rootNode = parser.getCodec().readTree(parser);
            if (rootNode == null) {
                return Optional.empty();
            }
            return Optional.of(parseFunction.apply(rootNode));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private Set<String> parseHashes(JsonNode rootNode) {
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

    private Transaction getGetTransactionDetails(String transactionHash) {
        Transaction transactionDetails = transactionService.getTransactionDetails(transactionHash);
        if (transactionDetails.isInvalid()) {
            logger.warn("Unable to find transaction {}", transactionHash);
        }
        return transactionDetails;
    }

    private boolean isSweepTransaction(Transaction transaction) {
        boolean hasSingleOutput = transaction.getOutputs().size() == 1;
        if (!hasSingleOutput && !transaction.equals(Transaction.UNKNOWN)) {
            logger.warn("Not a sweep transaction: {}", transaction);
        }
        return hasSingleOutput;
    }

    private Transaction addTransactionDescription(Transaction transaction) {
        transactionDescriptionService.set(transaction.getHash(), SWEEP_TRANSACTION_DESCRIPTION);
        return transaction;
    }

    private Transaction addAddressDescriptions(Transaction transaction) {
        String inputAddress = getInputAddress(transaction);
        AddressWithDescription addressWithDescription = addressDescriptionService.get(inputAddress);
        if (addressWithDescription.getDescription().isBlank()) {
            addressDescriptionService.set(inputAddress, DEFAULT_ADDRESS_DESCRIPTION);
        }
        addressDescriptionService.set(getOutputAddress(transaction), DEFAULT_ADDRESS_DESCRIPTION);
        return transaction;
    }

    @SuppressWarnings("PMD.LinguisticNaming")
    private Transaction setAddressesAsOwned(Transaction transaction) {
        addressOwnershipService.setAddressAsOwned(getInputAddress(transaction));
        addressOwnershipService.setAddressAsOwned(getOutputAddress(transaction));
        return transaction;
    }

    private String getOutputAddress(Transaction transaction) {
        return transaction.getOutputs().get(0).getAddress();
    }

    private String getInputAddress(Transaction transaction) {
        return transaction.getInputs().get(0).getAddress();
    }
}

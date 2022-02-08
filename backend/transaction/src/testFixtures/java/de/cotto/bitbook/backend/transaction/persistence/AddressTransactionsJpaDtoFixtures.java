package de.cotto.bitbook.backend.transaction.persistence;

import de.cotto.bitbook.backend.model.TransactionHash;

import java.util.Set;
import java.util.stream.Collectors;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;

public class AddressTransactionsJpaDtoFixtures {
    public static final AddressTransactionsJpaDto ADDRESS_TRANSACTIONS_JPA_DTO;

    static {
        ADDRESS_TRANSACTIONS_JPA_DTO = new AddressTransactionsJpaDto();
        ADDRESS_TRANSACTIONS_JPA_DTO.setAddress(ADDRESS_TRANSACTIONS.getAddress().toString());
        Set<String> hashes = ADDRESS_TRANSACTIONS.getTransactionHashes().stream()
                .map(TransactionHash::toString)
                .collect(Collectors.toSet());
        ADDRESS_TRANSACTIONS_JPA_DTO.setTransactionHashes(hashes);
        ADDRESS_TRANSACTIONS_JPA_DTO.setLastCheckedAtBlockheight(ADDRESS_TRANSACTIONS.getLastCheckedAtBlockHeight());
    }
}

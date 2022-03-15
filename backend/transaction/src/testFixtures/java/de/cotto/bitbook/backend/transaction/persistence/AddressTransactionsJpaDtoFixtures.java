package de.cotto.bitbook.backend.transaction.persistence;

import de.cotto.bitbook.backend.model.TransactionHash;

import java.util.Set;
import java.util.stream.Collectors;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.Chain.BTC;

public class AddressTransactionsJpaDtoFixtures {
    public static final AddressTransactionsJpaDto ADDRESS_TRANSACTIONS_JPA_DTO;

    static {
        ADDRESS_TRANSACTIONS_JPA_DTO = new AddressTransactionsJpaDto();
        ADDRESS_TRANSACTIONS_JPA_DTO.setAddress(ADDRESS_TRANSACTIONS.address().toString());
        ADDRESS_TRANSACTIONS_JPA_DTO.setChain(BTC.toString());
        Set<String> hashes = ADDRESS_TRANSACTIONS.transactionHashes().stream()
                .map(TransactionHash::toString)
                .collect(Collectors.toSet());
        ADDRESS_TRANSACTIONS_JPA_DTO.setTransactionHashes(hashes);
        ADDRESS_TRANSACTIONS_JPA_DTO.setLastCheckedAtBlockheight(ADDRESS_TRANSACTIONS.lastCheckedAtBlockHeight());
    }
}

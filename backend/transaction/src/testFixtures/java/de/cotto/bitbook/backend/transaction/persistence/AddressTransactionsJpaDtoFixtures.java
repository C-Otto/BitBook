package de.cotto.bitbook.backend.transaction.persistence;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;

public class AddressTransactionsJpaDtoFixtures {
    public static final AddressTransactionsJpaDto ADDRESS_TRANSACTIONS_JPA_DTO;

    static {
        ADDRESS_TRANSACTIONS_JPA_DTO = new AddressTransactionsJpaDto();
        ADDRESS_TRANSACTIONS_JPA_DTO.setAddress(ADDRESS_TRANSACTIONS.getAddress());
        ADDRESS_TRANSACTIONS_JPA_DTO.setTransactionHashes(ADDRESS_TRANSACTIONS.getTransactionHashes());
        ADDRESS_TRANSACTIONS_JPA_DTO.setLastCheckedAtBlockheight(ADDRESS_TRANSACTIONS.getLastCheckedAtBlockHeight());
    }
}

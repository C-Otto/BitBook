package de.cotto.bitbook.backend.transaction.persistence;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.TransactionHash;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS_2;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.transaction.persistence.AddressTransactionsJpaDtoFixtures.ADDRESS_TRANSACTIONS_JPA_DTO;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressTransactionsDaoImplTest {
    @InjectMocks
    private AddressTransactionsDaoImpl addressTransactionsDao;

    @Mock
    private AddressTransactionsRepository repository;

    @Test
    void getAddressTransactions_unknown() {
        AddressTransactions transaction = addressTransactionsDao.getAddressTransactions(ADDRESS, BTC);
        assertThat(transaction).isEqualTo(AddressTransactions.unknown(BTC));
    }

    @Test
    void getAddressTransactions() {
        when(repository.findById(AddressTransactionsJpaDtoId.fromModels(ADDRESS, BTC)))
                .thenReturn(Optional.of(ADDRESS_TRANSACTIONS_JPA_DTO));

        AddressTransactions transaction = addressTransactionsDao.getAddressTransactions(ADDRESS, BTC);

        assertThat(transaction).isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void saveAddressTransactions() {
        addressTransactionsDao.saveAddressTransactions(ADDRESS_TRANSACTIONS);
        verify(repository).save(argThat(
                dto -> ADDRESS_TRANSACTIONS.address().equals(new Address(dto.getAddress()))
        ));
        verify(repository).save(argThat(
                dto -> ADDRESS_TRANSACTIONS.lastCheckedAtBlockHeight() == dto.getLastCheckedAtBlockheight()
        ));
        verify(repository).save(argThat(
                dto -> ADDRESS_TRANSACTIONS.transactionHashes().equals(hashes(dto))
        ));
    }

    @Test
    void saveAddressTransactions_also_for_zero_transactions() {
        // we know that there are no known transactions for the address, which is useful information!
        // with this, we don't need to check this address again for a while
        addressTransactionsDao.saveAddressTransactions(new AddressTransactions(ADDRESS, Set.of(), 123, BTC));
        verify(repository).save(argThat(
                dto -> ADDRESS.equals(new Address(dto.getAddress()))
        ));
        verify(repository).save(argThat(
                dto -> 123 == dto.getLastCheckedAtBlockheight()
        ));
        verify(repository).save(argThat(
                dto -> Collections.emptySet().equals(hashes(dto))
        ));
    }

    @Test
    void getAddressesStartingWith() {
        String prefix = ADDRESS.toString().substring(0, 2);
        when(repository.findByAddressStartingWith(prefix)).thenReturn(List.of(ADDRESS::toString, ADDRESS_2::toString));
        assertThat(addressTransactionsDao.getAddressesStartingWith(prefix))
                .containsExactlyInAnyOrder(ADDRESS, ADDRESS_2);
    }

    private Set<TransactionHash> hashes(AddressTransactionsJpaDto dto) {
        return dto.getTransactionHashes().stream().map(TransactionHash::new).collect(toSet());
    }
}
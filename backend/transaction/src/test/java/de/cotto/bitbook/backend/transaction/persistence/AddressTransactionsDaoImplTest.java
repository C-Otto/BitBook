package de.cotto.bitbook.backend.transaction.persistence;

import de.cotto.bitbook.backend.model.AddressTransactions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_2;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.transaction.persistence.AddressTransactionsJpaDtoFixtures.ADDRESS_TRANSACTIONS_JPA_DTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressTransactionsDaoImplTest {
    @InjectMocks
    private AddressTransactionsDaoImpl addressTransactionsDao;

    @Mock
    private AddressTransactionsRepository repository;

    @Test
    void getAddressTransactions_unknown() {
        AddressTransactions transaction = addressTransactionsDao.getAddressTransactions(ADDRESS);
        assertThat(transaction).isEqualTo(AddressTransactions.UNKNOWN);
    }

    @Test
    void getAddressTransactions() {
        when(repository.findById(ADDRESS)).thenReturn(Optional.of(ADDRESS_TRANSACTIONS_JPA_DTO));

        AddressTransactions transaction = addressTransactionsDao.getAddressTransactions(ADDRESS);

        assertThat(transaction).isEqualTo(ADDRESS_TRANSACTIONS);
    }

    @Test
    void saveAddressTransactions() {
        addressTransactionsDao.saveAddressTransactions(ADDRESS_TRANSACTIONS);
        verify(repository).save(argThat(dto -> ADDRESS_TRANSACTIONS.getAddress().equals(dto.getAddress())));
        verify(repository).save(argThat(
                dto -> ADDRESS_TRANSACTIONS.getLastCheckedAtBlockHeight() == dto.getLastCheckedAtBlockheight()
        ));
        verify(repository).save(argThat(
                dto -> ADDRESS_TRANSACTIONS.getTransactionHashes().equals(dto.getTransactionHashes())
        ));
    }

    @Test
    void saveAddressTransactions_not_for_zero_transactions() {
        addressTransactionsDao.saveAddressTransactions(new AddressTransactions(ADDRESS, Set.of(), 123));
        verifyNoInteractions(repository);
    }

    @Test
    void getAddressesStartingWith() {
        String prefix = ADDRESS.substring(0, 2);
        when(repository.findByAddressStartingWith(prefix)).thenReturn(List.of(() -> ADDRESS, () -> ADDRESS_2));
        assertThat(addressTransactionsDao.getAddressesStartingWith(prefix))
                .containsExactlyInAnyOrder(ADDRESS, ADDRESS_2);
    }
}
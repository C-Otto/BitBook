package de.cotto.bitbook.backend.transaction.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionCompletionDaoImplTest {

    @InjectMocks
    private TransactionCompletionDaoImpl transactionDao;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AddressTransactionsRepository addressTransactionsRepository;

    private final String prefix = TRANSACTION_HASH.toString().substring(0, 3);

    @Test
    void completeFromTransactionDetails() {
        when(transactionRepository.findByHashStartingWith(prefix)).thenReturn(Set.of(TRANSACTION_HASH::toString));
        assertThat(transactionDao.completeFromTransactionDetails(prefix)).containsExactly(TRANSACTION_HASH);
    }

    @Test
    void completeFromAddressTransactionHashes() {
        when(addressTransactionsRepository.findTransactionHashesByPrefix(prefix))
                .thenReturn(Set.of(TRANSACTION_HASH.toString()));
        assertThat(transactionDao.completeFromAddressTransactionHashes(prefix)).containsExactly(TRANSACTION_HASH);
    }
}
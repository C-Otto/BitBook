package de.cotto.bitbook.backend.transaction.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionCompletionDaoImplTest {

    @InjectMocks
    private TransactionCompletionDaoImpl transactionDao;

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void getTransactionsStartingWith() {
        String prefix = TRANSACTION_HASH.substring(0, 3);
        when(transactionRepository.findByHashStartingWith(prefix)).thenReturn(Set.of(() -> TRANSACTION_HASH));
        assertThat(transactionDao.getTransactionHashesStartingWith(prefix)).containsExactly(TRANSACTION_HASH);
    }
}
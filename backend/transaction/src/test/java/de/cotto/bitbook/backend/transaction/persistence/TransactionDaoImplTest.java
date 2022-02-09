package de.cotto.bitbook.backend.transaction.persistence;

import de.cotto.bitbook.backend.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.util.Optional;

import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.persistence.TransactionJpaDtoFixtures.TRANSACTION_JPA_DTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionDaoImplTest {

    @InjectMocks
    private TransactionDaoImpl transactionDao;

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void getTransaction_unknown() {
        Transaction transaction = transactionDao.getTransaction(TRANSACTION_HASH);
        assertThat(transaction).isEqualTo(Transaction.UNKNOWN);
    }

    @Test
    void getTransaction() {
        when(transactionRepository.findById(TRANSACTION_HASH.toString())).thenReturn(Optional.of(TRANSACTION_JPA_DTO));

        Transaction transaction = transactionDao.getTransaction(TRANSACTION_HASH);

        assertThat(transaction).isEqualTo(TRANSACTION);
    }

    @Test
    void saveTransaction() {
        transactionDao.saveTransaction(TRANSACTION);
        verify(transactionRepository).save(argThat(dto -> TRANSACTION_HASH.toString().equals(dto.getHash())));
        verify(transactionRepository).save(argThat(dto -> BLOCK_HEIGHT == dto.getBlockHeight()));
        verify(transactionRepository).save(argThat(dto -> DATE_TIME.toEpochSecond(ZoneOffset.UTC) == dto.getTime()));
    }
}
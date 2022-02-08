package de.cotto.bitbook.backend.persistence;

import de.cotto.bitbook.backend.model.TransactionHash;
import de.cotto.bitbook.backend.model.TransactionWithDescription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("CPD-START")
@ExtendWith(MockitoExtension.class)
class TransactionWithDescriptionDaoImplTest {
    @InjectMocks
    private TransactionWithDescriptionDaoImpl dao;

    @Mock
    private TransactionWithDescriptionRepository repository;

    @Test
    void get() {
        TransactionHash transactionHash = new TransactionHash("xxx");
        String description = "description";
        when(repository.findById(transactionHash.toString())).thenReturn(
                Optional.of(new TransactionWithDescriptionJpaDto(transactionHash.toString(), description))
        );
        assertThat(dao.get(transactionHash)).isEqualTo(new TransactionWithDescription(transactionHash, description));
    }

    @Test
    void get_not_found() {
        TransactionHash transactionHash = new TransactionHash("xxx");
        when(repository.findById(transactionHash.toString())).thenReturn(Optional.empty());
        assertThat(dao.get(transactionHash)).isEqualTo(new TransactionWithDescription(transactionHash));
    }

    @Test
    void save() {
        TransactionHash transactionHash = new TransactionHash("xxx");
        String description = "description";
        dao.save(new TransactionWithDescription(transactionHash, description));
        verify(repository).save(argThat(dto -> dto.getTransactionHash().equals(transactionHash)));
        verify(repository).save(argThat(dto -> dto.getDescription().equals(description)));
    }

    @Test
    void remove() {
        dao.remove(new TransactionHash("a"));
        verify(repository).deleteById("a");
    }

    @Test
    void findWithDescriptionInfix() {
        String infix = "infix";
        when(repository.findByDescriptionContaining(infix))
                .thenReturn(Set.of(new TransactionWithDescriptionJpaDto("x", "y")));
        assertThat(dao.findWithDescriptionInfix(infix))
                .containsExactly(new TransactionWithDescription(new TransactionHash("x"), "y"));
    }
}
package de.cotto.bitbook.backend.persistence;

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

@ExtendWith(MockitoExtension.class)
class TransactionWithDescriptionDaoImplTest {
    @InjectMocks
    private TransactionWithDescriptionDaoImpl dao;

    @Mock
    private TransactionWithDescriptionRepository repository;

    @Test
    void get() {
        String transactionHash = "xxx";
        String description = "description";
        when(repository.findById(transactionHash)).thenReturn(
                Optional.of(new TransactionWithDescriptionJpaDto(transactionHash, description))
        );
        assertThat(dao.get(transactionHash)).isEqualTo(new TransactionWithDescription(transactionHash, description));
    }

    @Test
    void get_not_found() {
        String transactionHash = "xxx";
        when(repository.findById(transactionHash)).thenReturn(Optional.empty());
        assertThat(dao.get(transactionHash)).isEqualTo(new TransactionWithDescription(transactionHash));
    }

    @Test
    void save() {
        String transactionHash = "xxx";
        String description = "description";
        dao.save(new TransactionWithDescription(transactionHash, description));
        verify(repository).save(argThat(dto -> dto.getTransactionHash().equals(transactionHash)));
        verify(repository).save(argThat(dto -> dto.getDescription().equals(description)));
    }

    @Test
    void remove() {
        dao.remove("a");
        verify(repository).deleteById("a");
    }

    @Test
    void findWithDescriptionInfix() {
        String infix = "infix";
        when(repository.findByDescriptionContaining(infix))
                .thenReturn(Set.of(new TransactionWithDescriptionJpaDto("x", "y")));
        assertThat(dao.findWithDescriptionInfix(infix))
                .containsExactly(new TransactionWithDescription("x", "y"));
    }
}
package de.cotto.bitbook.backend;

import de.cotto.bitbook.backend.model.TransactionWithDescription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("CPD-START")
@ExtendWith(MockitoExtension.class)
class TransactionDescriptionServiceTest {
    private static final String TRANSACTION_HASH = "foo";

    @InjectMocks
    private TransactionDescriptionService service;

    @Mock
    private TransactionWithDescriptionDao dao;

    @Test
    void get() {
        TransactionWithDescription expected = new TransactionWithDescription(TRANSACTION_HASH);
        when(dao.get(TRANSACTION_HASH)).thenReturn(expected);
        assertThat(service.get(TRANSACTION_HASH)).isEqualTo(expected);
    }

    @Test
    void set() {
        String description = "bar";
        service.set(TRANSACTION_HASH, description);
        verify(dao).save(new TransactionWithDescription(TRANSACTION_HASH, description));
    }

    @Test
    void set_ignores_empty_string() {
        String description = "";
        service.set(TRANSACTION_HASH, description);
        verifyNoInteractions(dao);
    }

    @Test
    void set_ignores_blank_string() {
        String description = " ";
        service.set(TRANSACTION_HASH, description);
        verifyNoInteractions(dao);
    }

    @Test
    void remove() {
        service.remove(TRANSACTION_HASH);
        verify(dao).remove(TRANSACTION_HASH);
    }

    @Test
    void getWithDescriptionInfix() {
        String infix = "infix";
        TransactionWithDescription expected = new TransactionWithDescription("x", "y");
        when(dao.findWithDescriptionInfix(infix)).thenReturn(Set.of(expected));
        assertThat(service.getWithDescriptionInfix(infix)).containsExactly(expected);
    }

    @Test
    void getWithDescriptionInfix_too_short() {
        String infix = "ab";
        assertThat(service.getWithDescriptionInfix(infix)).isEmpty();
        verifyNoInteractions(dao);
    }
}
package de.cotto.bitbook.backend;

import de.cotto.bitbook.backend.model.AddressWithDescription;
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

@ExtendWith(MockitoExtension.class)
class AddressDescriptionServiceTest {
    private static final String ADDRESS = "foo";

    @InjectMocks
    private AddressDescriptionService service;

    @Mock
    private AddressWithDescriptionDao addressWithDescriptionDao;

    @Test
    void get() {
        AddressWithDescription expected = new AddressWithDescription(ADDRESS);
        when(addressWithDescriptionDao.get(ADDRESS)).thenReturn(expected);
        assertThat(service.get(ADDRESS)).isEqualTo(expected);
    }

    @Test
    void set() {
        String description = "bar";
        service.set(ADDRESS, description);
        verify(addressWithDescriptionDao).save(new AddressWithDescription(ADDRESS, description));
    }

    @Test
    void set_ignores_empty_string() {
        String description = "";
        service.set(ADDRESS, description);
        verifyNoInteractions(addressWithDescriptionDao);
    }

    @Test
    void set_ignores_blank_string() {
        String description = " ";
        service.set(ADDRESS, description);
        verifyNoInteractions(addressWithDescriptionDao);
    }

    @Test
    void remove() {
        service.remove(ADDRESS);
        verify(addressWithDescriptionDao).remove(ADDRESS);
    }

    @Test
    void getWithDescriptionInfix() {
        String infix = "infix";
        AddressWithDescription expected = new AddressWithDescription("x", "y");
        when(addressWithDescriptionDao.findWithDescriptionInfix(infix)).thenReturn(Set.of(expected));
        assertThat(service.getWithDescriptionInfix(infix)).containsExactly(expected);
    }

    @Test
    void getWithDescriptionInfix_too_short() {
        String infix = "ab";
        assertThat(service.getWithDescriptionInfix(infix)).isEmpty();
        verifyNoInteractions(addressWithDescriptionDao);
    }
}
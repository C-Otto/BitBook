package de.cotto.bitbook.backend.persistence;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressWithDescription;
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
class AddressWithDescriptionDaoImplTest {
    @InjectMocks
    private AddressWithDescriptionDaoImpl dao;

    @Mock
    private AddressWithDescriptionRepository repository;

    @Test
    void get() {
        Address address = new Address("xxx");
        String description = "description";
        when(repository.findById(address.toString())).thenReturn(
                Optional.of(new AddressWithDescriptionJpaDto(address.toString(), description))
        );
        assertThat(dao.get(address)).isEqualTo(new AddressWithDescription(address, description));
    }

    @Test
    void get_not_found() {
        Address address = new Address("xxx");
        when(repository.findById(address.toString())).thenReturn(Optional.empty());
        assertThat(dao.get(address)).isEqualTo(new AddressWithDescription(address));
    }

    @Test
    void save() {
        Address address = new Address("xxx");
        String description = "description";
        dao.save(new AddressWithDescription(address, description));
        verify(repository).save(argThat(dto -> dto.getAddress().equals(address.toString())));
        verify(repository).save(argThat(dto -> dto.getDescription().equals(description)));
    }

    @Test
    void remove() {
        dao.remove(new Address("a"));
        verify(repository).deleteById("a");
    }

    @Test
    void findWithDescriptionInfix() {
        String infix = "infix";
        when(repository.findByDescriptionContaining(infix))
                .thenReturn(Set.of(new AddressWithDescriptionJpaDto("x", "y")));
        assertThat(dao.findWithDescriptionInfix(infix))
                .containsExactly(new AddressWithDescription(new Address("x"), "y"));
    }
}
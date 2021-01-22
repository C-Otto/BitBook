package de.cotto.bitbook.backend.persistence;

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

@ExtendWith(MockitoExtension.class)
class AddressWithDescriptionDaoImplTest {
    @InjectMocks
    private AddressWithDescriptionDaoImpl dao;

    @Mock
    private AddressWithDescriptionRepository repository;

    @Test
    void get() {
        String address = "xxx";
        String description = "description";
        when(repository.findById(address)).thenReturn(
                Optional.of(new AddressWithDescriptionJpaDto(address, description))
        );
        assertThat(dao.get(address)).isEqualTo(new AddressWithDescription(address, description));
    }

    @Test
    void get_not_found() {
        String address = "xxx";
        when(repository.findById(address)).thenReturn(Optional.empty());
        assertThat(dao.get(address)).isEqualTo(new AddressWithDescription(address));
    }

    @Test
    void save() {
        String address = "xxx";
        String description = "description";
        dao.save(new AddressWithDescription(address, description));
        verify(repository).save(argThat(dto -> dto.getAddress().equals(address)));
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
                .thenReturn(Set.of(new AddressWithDescriptionJpaDto("x", "y")));
        assertThat(dao.findWithDescriptionInfix(infix))
                .containsExactly(new AddressWithDescription("x", "y"));
    }
}
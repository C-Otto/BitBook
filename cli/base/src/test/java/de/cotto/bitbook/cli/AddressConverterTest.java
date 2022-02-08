package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.model.Address;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressConverterTest {
    @Mock
    private AddressCompletionProvider addressCompletionProvider;

    @InjectMocks
    private AddressConverter addressConverter;

    @Test
    void convert() {
        assertThat(addressConverter.convert("x")).isEqualTo(new CliAddress("x"));
        verify(addressCompletionProvider, never()).completeIfUnique(anyString());
    }

    @Test
    void autocompletes_if_ends_in_ellipsis() {
        when(addressCompletionProvider.completeIfUnique("x…")).thenReturn(Optional.of(new Address("xyz")));
        assertThat(addressConverter.convert("x…")).isEqualTo(new CliAddress("xyz"));
    }

    @Test
    void tries_to_autocompletes_but_not_found() {
        assertThat(addressConverter.convert("x…")).isEqualTo(new CliAddress("x…"));
    }
}
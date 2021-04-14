package de.cotto.bitbook.lnd.cli;

import de.cotto.bitbook.lnd.LndService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LndCommandsTest {
    @InjectMocks
    private LndCommands lndCommands;

    @Mock
    private LndService lndService;

    @Test
    void lndAddFromSweeps() throws IOException {
        when(lndService.lndAddFromSweeps(any())).thenReturn(123L);
        String json = "{\"foo\": \"bar\"}";
        File file = File.createTempFile("temp", "bitbook");
        Files.writeString(file.toPath(), json);

        assertThat(lndCommands.lndAddFromSweeps(file)).isEqualTo("Added information for 123 sweep transactions");

        verify(lndService).lndAddFromSweeps(json);
    }

    @Test
    void lndAddFromSweeps_failure() throws IOException {
        when(lndService.lndAddFromSweeps(any())).thenReturn(0L);
        File file = File.createTempFile("temp", "bitbook");

        assertThat(lndCommands.lndAddFromSweeps(file)).isEqualTo("Unable to find sweep transactions in file");
    }
}
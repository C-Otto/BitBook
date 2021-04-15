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
    private static final String JSON = "{\"hello\": \"kitty\"}";

    @InjectMocks
    private LndCommands lndCommands;

    @Mock
    private LndService lndService;

    @Test
    void lndAddFromSweeps() throws IOException {
        when(lndService.addFromSweeps(any())).thenReturn(123L);
        File file = createTempFileWithContent();

        assertThat(lndCommands.lndAddFromSweeps(file)).isEqualTo("Added information for 123 sweep transactions");

        verify(lndService).addFromSweeps(JSON);
    }

    @Test
    void lndAddFromSweeps_failure() throws IOException {
        when(lndService.addFromSweeps(any())).thenReturn(0L);
        File file = createTempFile();

        assertThat(lndCommands.lndAddFromSweeps(file)).isEqualTo("Unable to find sweep transactions in file");
    }

    @Test
    void addFromUnspentOutputs() throws IOException {
        when(lndService.addFromUnspentOutputs(any())).thenReturn(123L);
        File file = createTempFileWithContent();

        assertThat(lndCommands.lndAddFromUnspentOutputs(file)).isEqualTo("Marked 123 addresses as owned by lnd");

        verify(lndService).addFromUnspentOutputs(JSON);
    }

    @Test
    void lndAddFromUnspentOutputs_failure() throws IOException {
        when(lndService.addFromUnspentOutputs(any())).thenReturn(0L);
        File file = createTempFile();

        assertThat(lndCommands.lndAddFromUnspentOutputs(file))
                .isEqualTo("Unable to find unspent output address in file");
    }

    @Test
    void lndAddFromChannels() throws IOException {
        when(lndService.addFromChannels(any())).thenReturn(123L);
        File file = createTempFileWithContent();

        assertThat(lndCommands.lndAddFromChannels(file)).isEqualTo("Added information for 123 channels");

        verify(lndService).addFromChannels(JSON);
    }

    @Test
    void lndAddFromChannels_failure() throws IOException {
        when(lndService.addFromChannels(any())).thenReturn(0L);
        File file = createTempFile();

        assertThat(lndCommands.lndAddFromChannels(file)).isEqualTo("Unable to find channel in file");
    }

    @Test
    void lndAddFromClosedChannels() throws IOException {
        when(lndService.addFromClosedChannels(any())).thenReturn(123L);
        File file = createTempFileWithContent();

        assertThat(lndCommands.lndAddFromClosedChannels(file)).isEqualTo("Added information for 123 closed channels");

        verify(lndService).addFromClosedChannels(JSON);
    }

    @Test
    void lndAddFromClosedChannels_failure() throws IOException {
        when(lndService.addFromClosedChannels(any())).thenReturn(0L);
        File file = createTempFile();

        assertThat(lndCommands.lndAddFromClosedChannels(file)).isEqualTo("Unable to find closed channel in file");
    }

    @Test
    void lndAddFromOnchainTransactions() throws IOException {
        when(lndService.addFromOnchainTransactions(any())).thenReturn(123L);
        File file = createTempFileWithContent();

        assertThat(lndCommands.lndAddFromOnchainTransactions(file))
                .isEqualTo("Added information from 123 transactions");

        verify(lndService).addFromOnchainTransactions(JSON);
    }

    @Test
    void lndAddFromOnchainTransactions_failure() throws IOException {
        when(lndService.addFromOnchainTransactions(any())).thenReturn(0L);
        File file = createTempFile();

        assertThat(lndCommands.lndAddFromOnchainTransactions(file))
                .isEqualTo("Unable to find usable transactions in file");
    }

    private File createTempFileWithContent() throws IOException {
        File file = createTempFile();
        Files.writeString(file.toPath(), JSON);
        return file;
    }

    private File createTempFile() throws IOException {
        return File.createTempFile("temp", "bitbook");
    }
}
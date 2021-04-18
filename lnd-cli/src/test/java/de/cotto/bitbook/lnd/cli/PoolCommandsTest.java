package de.cotto.bitbook.lnd.cli;

import de.cotto.bitbook.lnd.PoolService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PoolCommandsTest {
    private static final String JSON = "{\"hello\": \"kitty\"}";

    @InjectMocks
    private PoolCommands poolCommands;

    @Mock
    private PoolService poolService;

    @Test
    void poolAddFromLeases() throws IOException {
        when(poolService.addFromLeases(any())).thenReturn(123L);
        File file = TempFileUtil.createTempFileWithContent(JSON);

        assertThat(poolCommands.poolAddFromLeases(file)).isEqualTo("Added information for 123 leases");

        verify(poolService).addFromLeases(JSON);
    }

    @Test
    void poolAddFromLeases_failure() throws IOException {
        when(poolService.addFromLeases(any())).thenReturn(0L);
        File file = TempFileUtil.createTempFile();

        assertThat(poolCommands.poolAddFromLeases(file)).isEqualTo("Unable to find leases in file");
    }
}
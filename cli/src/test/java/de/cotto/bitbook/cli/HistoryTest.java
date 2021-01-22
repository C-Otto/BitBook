package de.cotto.bitbook.cli;

import org.jline.reader.LineReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HistoryTest {

    private final LineReader lineReader = mock(LineReader.class);
    private History historyPath;

    @BeforeEach
    void setUp() {
        historyPath = new History(lineReader, "historyPath");
    }

    @Test
    void closeInteractsWithLineReader() {
        historyPath.close();
        verify(lineReader, times(2)).getVariables();
    }

    @Test
    void ignoresIoException(@TempDir Path tempDir) {
        assertThat(tempDir.toFile().setWritable(false)).isTrue();
        when(lineReader.getVariables()).thenReturn(Map.of("history-file", tempDir));
        assertThatCode(() -> historyPath.close()).doesNotThrowAnyException();
    }
}
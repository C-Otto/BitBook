package de.cotto.bitbook.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;
import org.springframework.shell.ExitRequest;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class QuitCommandTest {

    private final ExecutorConfigurationSupport executorConfigurationSupport = mock(ExecutorConfigurationSupport.class);
    private final QuitCommand quitCommand = new QuitCommand(Set.of(executorConfigurationSupport));
    private History history;

    @BeforeEach
    void setUp() {
        history = mock(History.class);
        setHistoryField(quitCommand, history);
    }

    @Test
    void throwsException() {
        assertThatExceptionOfType(ExitRequest.class).isThrownBy(
                quitCommand::quit
        );
    }

    @Test
    void invokesHistoryClose() {
        assertThatExceptionOfType(ExitRequest.class).isThrownBy(
                quitCommand::quit
        );
        verify(history).close();
    }

    @Test
    void invokesAsyncShutdown() {
        assertThatExceptionOfType(ExitRequest.class).isThrownBy(
                quitCommand::quit
        );
        verify(executorConfigurationSupport).shutdown();
    }

    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    private void setHistoryField(QuitCommand quitCommand, History history) {
        Field historyField = Objects.requireNonNull(ReflectionUtils.findField(QuitCommand.class, "history"));
        historyField.setAccessible(true);
        ReflectionUtils.setField(historyField, quitCommand, history);
    }
}
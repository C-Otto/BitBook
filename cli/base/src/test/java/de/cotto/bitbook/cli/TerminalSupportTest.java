package de.cotto.bitbook.cli;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TerminalSupportTest {
    private TerminalSupport terminalSupport;

    @Mock
    private Terminal terminal;

    @Mock
    private LineReader lineReader;

    @Mock
    private PromptChangeListener promptChangeListener;

    @Mock
    private PrintWriter writer;

    @BeforeEach
    void setUp() {
        when(terminal.writer()).thenReturn(writer);
        terminalSupport = new TerminalSupport(terminal, lineReader, promptChangeListener);
    }

    @Test
    void write() {
        terminalSupport.write("x");
        InOrder inOrder = Mockito.inOrder(writer);
        inOrder.verify(writer).println("x");
        inOrder.verify(writer).flush();
    }

    @Test
    void request_prompt() {
        terminalSupport.request("?");
        verify(lineReader).readLine("? ");
    }

    @Test
    void request_empty() {
        when(lineReader.readLine(anyString())).thenReturn("");
        assertThat(terminalSupport.request("?")).isEmpty();
    }

    @Test
    void request_blank() {
        when(lineReader.readLine(anyString())).thenReturn("\t \r\n");
        assertThat(terminalSupport.request("?")).isEmpty();
    }

    @Test
    void request() {
        when(lineReader.readLine(anyString())).thenReturn("hello");
        assertThat(terminalSupport.request("?")).contains("hello");
    }

    @Test
    void changePrompt() {
        terminalSupport.changePrompt("x");
        verify(promptChangeListener).changePrompt("x");
    }

    @Test
    void changePromptToDefault() {
        terminalSupport.changePromptToDefault();
        verify(promptChangeListener).changePromptToDefault();
    }
}
package de.cotto.bitbook.cli;

import org.apache.commons.lang3.StringUtils;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.util.Optional;

@Component
public class TerminalSupport {
    private final LineReader lineReader;
    private final PromptChangeListener promptChangeListener;
    private final PrintWriter writer;

    public TerminalSupport(Terminal terminal, LineReader lineReader, PromptChangeListener promptChangeListener) {
        this.lineReader = lineReader;
        this.promptChangeListener = promptChangeListener;
        writer = terminal.writer();
    }

    public void write(String text) {
        writer.println(text);
        writer.flush();
    }

    public Optional<String> request(String prompt) {
        String answer = lineReader.readLine(prompt + " ");
        if (StringUtils.isBlank(answer)) {
            return Optional.empty();
        } else {
            return Optional.of(answer);
        }
    }

    public void changePrompt(String newPrompt) {
        promptChangeListener.changePrompt(newPrompt);
    }

    public void changePromptToDefault() {
        promptChangeListener.changePromptToDefault();
    }
}

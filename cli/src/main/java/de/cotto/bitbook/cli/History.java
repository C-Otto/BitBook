package de.cotto.bitbook.cli;

import org.jline.reader.LineReader;
import org.jline.reader.impl.history.DefaultHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;

@Component
public class History extends DefaultHistory {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public History(LineReader lineReader, @Value("${spring.application.name:spring-shell}.log") String historyPath) {
        super(getLineReaderWithSetPath(lineReader, historyPath));
    }

    private static LineReader getLineReaderWithSetPath(LineReader lineReader, String historyPath) {
        lineReader.setVariable(LineReader.HISTORY_FILE, Paths.get(historyPath));
        return lineReader;
    }

    public void close() {
        try {
            save();
        } catch (IOException e) {
            logger.warn("Unable to save history", e);
        }
    }
}

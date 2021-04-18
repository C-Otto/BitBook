package de.cotto.bitbook.lnd.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class TempFileUtil {

    private TempFileUtil() {
        // utility class
    }

    public static File createTempFileWithContent(String content) throws IOException {
        File file = createTempFile();
        Files.writeString(file.toPath(), content);
        return file;
    }

    public static File createTempFile() throws IOException {
        return File.createTempFile("temp", "bitbook");
    }
}

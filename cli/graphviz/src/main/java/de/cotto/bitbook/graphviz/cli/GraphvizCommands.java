package de.cotto.bitbook.graphviz.cli;

import de.cotto.bitbook.graphviz.GraphvizService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class GraphvizCommands {
    private final GraphvizService graphvizService;

    public GraphvizCommands(GraphvizService graphvizService) {
        this.graphvizService = graphvizService;
    }

    @ShellMethod("TODO")
    public String createDottyFile() {
        graphvizService.createDottyFile();
        return "OK";
    }
}

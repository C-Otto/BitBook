package de.cotto.bitbook.cli;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.commands.Quit;

import java.util.Set;

@ShellComponent
public class QuitCommand implements Quit.Command {

    private final Set<ExecutorConfigurationSupport> executors;

    @Lazy

    @Autowired
    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    private History history;

    public QuitCommand(Set<ExecutorConfigurationSupport> executors) {
        // default constructor
        this.executors = executors;
    }

    @ShellMethod(value = "Exit the shell.", key = {"quit", "exit"})
    public void quit() {
        executors.forEach(ExecutorConfigurationSupport::shutdown);
        history.close();
        throw new ExitRequest();
    }

}
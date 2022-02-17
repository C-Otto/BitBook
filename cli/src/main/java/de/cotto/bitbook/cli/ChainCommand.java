package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.model.Chain;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class ChainCommand {
    private final SelectedChain selectedChain;

    public ChainCommand(SelectedChain selectedChain) {
        this.selectedChain = selectedChain;
    }

    @ShellMethod("Use the given chain for future commands")
    public void selectChain(Chain chain) {
        selectedChain.selectChain(chain);
    }

}
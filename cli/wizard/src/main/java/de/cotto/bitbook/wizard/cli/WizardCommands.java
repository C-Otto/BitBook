package de.cotto.bitbook.wizard.cli;

import de.cotto.bitbook.cli.PromptChangeListener;
import de.cotto.bitbook.wizard.WizardService;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class WizardCommands {
    private final WizardService wizardService;
    private final PromptChangeListener promptChangeListener;

    public WizardCommands(WizardService wizardService, PromptChangeListener promptChangeListener) {
        this.wizardService = wizardService;
        this.promptChangeListener = promptChangeListener;
    }

    @ShellMethod("Start the wizard which helps you complete the ownership information")
    public void wizard() {
        wizardService.enableWizard();
        promptChangeListener.changePrompt("wizard$ ");
    }

    @ShellMethod("Exit the wizard")
    public void exitWizard() {
        wizardService.disableWizard();
        promptChangeListener.changePromptToDefault();
    }

    public Availability exitWizardAvailability() {
        if (wizardService.isEnabled()) {
            return Availability.available();
        }
        return Availability.unavailable("wizard is not active");
    }
}

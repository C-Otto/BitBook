package de.cotto.bitbook.wizard.cli;

import de.cotto.bitbook.cli.PromptChangeListener;
import de.cotto.bitbook.wizard.WizardService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WizardCommandsTest {
    @InjectMocks
    private WizardCommands wizardCommands;

    @Mock
    private WizardService wizardService;

    @Mock
    private PromptChangeListener promptChangeListener;

    @Nested
    class WizardDisabled {
        @Test
        void enables_wizard() {
            wizardCommands.wizard();
            verify(wizardService).enableWizard();
        }

        @Test
        void exit_wizard_command_initially_not_available() {
            assertThat(wizardCommands.exitWizardAvailability().isAvailable()).isEqualTo(false);
            assertThat(wizardCommands.exitWizardAvailability().getReason()).isEqualTo("wizard is not active");
        }
    }
    
    @Nested
    class WizardEnabled {
        @Test
        void changes_prompt() {
            wizardCommands.wizard();
            verify(promptChangeListener).changePrompt("wizard$ ");
        }

        @Test
        void exit_wizard_command_available() {
            when(wizardService.isEnabled()).thenReturn(true);
            assertThat(wizardCommands.exitWizardAvailability().isAvailable()).isEqualTo(true);
        }

        @Test
        void exit_wizard_changes_prompt_to_default() {
            wizardCommands.exitWizard();
            verify(promptChangeListener).changePromptToDefault();
        }

        @Test
        void exit_wizard_notifies_service() {
            wizardCommands.exitWizard();
            verify(wizardService).disableWizard();
        }
    }
}
package de.cotto.bitbook.wizard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class WizardServiceTest {
    @InjectMocks
    private WizardService wizardService;

    @Test
    void isEnabled() {
        assertThat(wizardService.isEnabled()).isFalse();
    }

    @Test
    void enableWizard() {
        wizardService.enableWizard();
        assertThat(wizardService.isEnabled()).isTrue();
    }

    @Test
    void disableWizard() {
        wizardService.disableWizard();
        assertThat(wizardService.isEnabled()).isFalse();
    }
}
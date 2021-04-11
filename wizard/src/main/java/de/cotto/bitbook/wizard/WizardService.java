package de.cotto.bitbook.wizard;

import org.springframework.stereotype.Component;

@Component
public class WizardService {
    private boolean enabled;

    public WizardService() {
        // default constructor
    }

    public void enableWizard() {
        enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void disableWizard() {
        enabled = false;
    }
}

package de.cotto.bitbook.cli;

public interface PromptChangeListener {
    void changePrompt(String newState);

    void changePromptToDefault();
}

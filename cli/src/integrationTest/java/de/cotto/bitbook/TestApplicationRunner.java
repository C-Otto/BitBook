package de.cotto.bitbook;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
public class TestApplicationRunner implements ApplicationRunner {

    public TestApplicationRunner() {
        // default constructor
    }

    @Override
    public void run(ApplicationArguments args) {
        // empty to avoid issues in tests
    }
}
package de.cotto.bitbook;

import de.cotto.bitbook.ownership.cli.OwnershipCommands;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TransactionsApplicationIT {
    @Autowired
    private OwnershipCommands ownershipCommands;

    @Test
    void contextLoads() {
        assertThat(ownershipCommands).isNotNull();
    }

}

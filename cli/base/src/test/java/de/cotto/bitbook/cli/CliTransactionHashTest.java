package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.model.TransactionHash;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CliTransactionHashTest {

    private static final TransactionHash VALID_HASH =
            new TransactionHash("6a79f4d09235e9c1db83260c7b4e9cd683135bd9f646e764337c229c1bb6a4d6");

    @Test
    void errorMessage() {
        assertThat(CliTransactionHash.ERROR_MESSAGE).isEqualTo("Expected: 64 hex characters");
    }

    @Test
    void getTransactionHash() {
        assertThat(new CliTransactionHash(VALID_HASH).getTransactionHash()).isEqualTo(VALID_HASH);
    }

    @Test
    void getTransactionHash_sanitized() {
        assertThat(new CliTransactionHash("\t" + VALID_HASH + ",:! ").getTransactionHash())
                .isEqualTo(VALID_HASH);
    }

    @Test
    void getTransactionHash_invalid_character() {
        CliTransactionHash invalid =
                new CliTransactionHash("6x79f4d09235e9c1db83260c7b4e9cd683135bd9f646e764337c229c1bb6a4d6");
        assertThat(invalid.getTransactionHash()).isEqualTo(TransactionHash.NONE);
    }

    @Test
    void getTransactionHash_invalid_tooShort() {
        CliTransactionHash invalid =
                new CliTransactionHash("6a79f4d09235e9c1db83260c7b4e9cd683135bd9f646e764337c229c1bb6a4d");
        assertThat(invalid.getTransactionHash()).isEqualTo(TransactionHash.NONE);
    }

    @Test
    void splits_on_no_breaking_space() {
        assertThat(new CliTransactionHash(VALID_HASH + "\u00a0xxx").getTransactionHash()).isEqualTo(VALID_HASH);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(CliTransactionHash.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        assertThat(new CliTransactionHash(VALID_HASH)).hasToString(VALID_HASH.toString());
    }

    @Test
    void testToString_garbage() {
        assertThat(new CliTransactionHash("    !" + VALID_HASH + ":\u00a0xxx")).hasToString(VALID_HASH.toString());
    }
}
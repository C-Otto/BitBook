package de.cotto.bitbook.backend.transaction.persistence;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class TransactionJpaDtoIdTest {
    @Test
    void testEquals() {
        EqualsVerifier.simple().forClass(TransactionJpaDtoId.class).verify();
    }
}
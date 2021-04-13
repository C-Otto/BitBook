package de.cotto.bitbook.backend.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("CPD-START")
class TransationWithDescriptionTest {
    private static final String TOO_LONG =
            "abcaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaXIIIIIIIIIIIIIIIIIZ";
    private static final String SHORTENED_40 = "abcaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaX…";
    private static final TransactionWithDescription TRANSACTION_WITH_DESCRIPTION =
            create("x", "y");

    @Test
    void getTransactionHash() {
        assertThat(TRANSACTION_WITH_DESCRIPTION.getTransactionHash()).isEqualTo("x");
    }

    @Test
    void getDescription() {
        assertThat(TRANSACTION_WITH_DESCRIPTION.getDescription()).isEqualTo("y");
    }

    @Test
    void compareTo_smaller_description() {
        assertThat(create("z", "a").compareTo(create("a", "z"))).isLessThan(0);
    }

    @Test
    void compareTo_same_description_smaller_hash() {
        assertThat(create("a", "y").compareTo(create("z", "y"))).isLessThan(0);
    }

    @Test
    void compareTo_same_description_same_hash() {
        assertThat(create("x", "y").compareTo(TRANSACTION_WITH_DESCRIPTION)).isEqualTo(0);
    }

    @Test
    void compareTo_same_description_larger_hash() {
        assertThat(create("z", "y").compareTo(create("a", "y"))).isGreaterThan(0);
    }

    @Test
    void compareTo_larger_description() {
        assertThat(create("a", "z").compareTo(create("z", "a"))).isGreaterThan(0);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(TransactionWithDescription.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        String transactionHash = TRANSACTION_WITH_DESCRIPTION.getTransactionHash();
        String formattedDescription = TRANSACTION_WITH_DESCRIPTION.getDescription();
        assertThat(TRANSACTION_WITH_DESCRIPTION).hasToString(transactionHash + " " + formattedDescription);
    }

    @Test
    void testToString_long_description() {
        TransactionWithDescription transactionWithDescription = new TransactionWithDescription(
                TRANSACTION_WITH_DESCRIPTION.getTransactionHash(),
                TOO_LONG
        );
        assertThat(transactionWithDescription).hasToString(
                transactionWithDescription.getTransactionHash() +
                " " +
                SHORTENED_40
        );
    }

    @Test
    void testToString_without_description() {
        assertThat(new TransactionWithDescription("x"))
                .hasToString("x ");
    }

    @Test
    void getDescription_without_description() {
        assertThat(new TransactionWithDescription("x").getDescription()).isEqualTo("");
    }

    @Test
    void getFormattedDescription() {
        assertThat(TRANSACTION_WITH_DESCRIPTION.getFormattedDescription())
                .isEqualTo(TRANSACTION_WITH_DESCRIPTION.getDescription());
    }

    @Test
    void getFormattedDescription_long() {
        assertThat(new TransactionWithDescription(
                TRANSACTION_WITH_DESCRIPTION.getTransactionHash(),
                TOO_LONG
        ).getFormattedDescription()).isEqualTo(SHORTENED_40);
    }

    private static TransactionWithDescription create(String transactionHash, String description) {
        return new TransactionWithDescription(transactionHash, description);
    }
}
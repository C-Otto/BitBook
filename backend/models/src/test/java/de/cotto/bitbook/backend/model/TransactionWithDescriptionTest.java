package de.cotto.bitbook.backend.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("CPD-START")
class TransactionWithDescriptionTest {
    private static final String TOO_LONG =
            "abcaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaXIIIIIIIIIIIIIIIIIZ";
    private static final String SHORTENED_40 = "abcaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaX…";
    private static final TransactionWithDescription TRANSACTION_WITH_DESCRIPTION =
            create(new TransactionHash("x"), "y");
    private static final TransactionHash HASH_Z = new TransactionHash("z");
    private static final TransactionHash HASH_A = new TransactionHash("a");

    @Test
    void getTransactionHash() {
        assertThat(TRANSACTION_WITH_DESCRIPTION.getTransactionHash()).isEqualTo(new TransactionHash("x"));
    }

    @Test
    void getDescription() {
        assertThat(TRANSACTION_WITH_DESCRIPTION.getDescription()).isEqualTo("y");
    }

    @Test
    void compareTo_smaller_description() {
        assertThat(create(HASH_Z, "a").compareTo(create(HASH_A, "z"))).isLessThan(0);
    }

    @Test
    void compareTo_same_description_smaller_hash() {
        assertThat(create(HASH_A, "y").compareTo(create(HASH_Z, "y"))).isLessThan(0);
    }

    @Test
    void compareTo_same_description_same_hash() {
        assertThat(TRANSACTION_WITH_DESCRIPTION.compareTo(create(new TransactionHash("x"), "y"))).isEqualTo(0);
    }

    @Test
    void compareTo_same_description_larger_hash() {
        assertThat(create(HASH_Z, "y").compareTo(create(HASH_A, "y"))).isGreaterThan(0);
    }

    @Test
    void compareTo_larger_description() {
        assertThat(create(HASH_A, "z").compareTo(create(HASH_Z, "a"))).isGreaterThan(0);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(TransactionWithDescription.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        TransactionHash transactionHash = TRANSACTION_WITH_DESCRIPTION.getTransactionHash();
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
    void testToString_max_length() {
        TransactionWithDescription transactionWithDescription = new TransactionWithDescription(
                TRANSACTION_WITH_DESCRIPTION.getTransactionHash(),
                "abcaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaXZ"
        );
        assertThat(transactionWithDescription).hasToString(
                transactionWithDescription.getTransactionHash() +
                " " +
                "abcaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaXZ"
        );
    }

    @Test
    void testToString_without_description() {
        assertThat(new TransactionWithDescription(new TransactionHash("x")))
                .hasToString("x ");
    }

    @Test
    void getDescription_without_description() {
        assertThat(new TransactionWithDescription(new TransactionHash("x")).getDescription()).isEqualTo("");
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

    private static TransactionWithDescription create(TransactionHash transactionHash, String description) {
        return new TransactionWithDescription(transactionHash, description);
    }
}
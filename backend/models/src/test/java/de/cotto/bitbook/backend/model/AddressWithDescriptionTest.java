package de.cotto.bitbook.backend.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressWithDescriptionTest {
    private static final String TOO_LONG =
            "abcaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaXaaaaXIIIIIIIIIIIIIIIIIZ";
    private static final String SHORTENED_45 = "abcaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaXaaaaX…";
    private static final String SHORTENED_40 = "abcaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaX…";
    private static final AddressWithDescription ADDRESS_WITH_DESCRIPTION =
            create(new Address("x"), "y");

    @Test
    void getAddress() {
        assertThat(ADDRESS_WITH_DESCRIPTION.getAddress()).isEqualTo(new Address("x"));
    }

    @Test
    void getDescription() {
        assertThat(ADDRESS_WITH_DESCRIPTION.getDescription()).isEqualTo("y");
    }

    @Test
    void compareTo_smaller_description() {
        assertThat(create(new Address("z"), "a").compareTo(create(new Address("a"), "z"))).isLessThan(0);
    }

    @Test
    void compareTo_same_description_smaller_address() {
        assertThat(create(new Address("a"), "y").compareTo(create(new Address("z"), "y"))).isLessThan(0);
    }

    @Test
    void compareTo_same_description_same_address() {
        assertThat(ADDRESS_WITH_DESCRIPTION.compareTo(create(new Address("x"), "y"))).isEqualTo(0);
    }

    @Test
    void compareTo_same_description_larger_address() {
        assertThat(create(new Address("z"), "y").compareTo(create(new Address("a"), "y"))).isGreaterThan(0);
    }

    @Test
    void compareTo_larger_description() {
        assertThat(create(new Address("a"), "z").compareTo(create(new Address("z"), "a"))).isGreaterThan(0);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(AddressWithDescription.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        String formattedAddress = StringUtils.leftPad(ADDRESS_WITH_DESCRIPTION.getAddress().toString(), 45);
        String formattedDescription = ADDRESS_WITH_DESCRIPTION.getDescription();
        assertThat(ADDRESS_WITH_DESCRIPTION).hasToString(formattedAddress + " " + formattedDescription);
    }

    @Test
    void testToString_long_address() {
        AddressWithDescription addressWithDescription = new AddressWithDescription(
                new Address(TOO_LONG),
                ADDRESS_WITH_DESCRIPTION.getDescription()
        );
        assertThat(addressWithDescription).hasToString(
                SHORTENED_45 +
                " " +
                addressWithDescription.getDescription()
        );
    }

    @Test
    void testToString_max_length() {
        AddressWithDescription addressWithDescription = new AddressWithDescription(
                new Address("abcaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaXaaaaXZ"),
                ADDRESS_WITH_DESCRIPTION.getDescription()
        );
        assertThat(addressWithDescription).hasToString(
                "abcaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaXaaaaXZ" +
                " " +
                addressWithDescription.getDescription()
        );
    }

    @Test
    void testToString_long_description() {
        AddressWithDescription addressWithDescription = new AddressWithDescription(
                ADDRESS_WITH_DESCRIPTION.getAddress(),
                TOO_LONG
        );
        assertThat(addressWithDescription).hasToString(
                StringUtils.leftPad(addressWithDescription.getAddress().toString(), 45) +
                " " +
                SHORTENED_40
        );
    }

    @Test
    void testToString_without_description() {
        assertThat(new AddressWithDescription(new Address("x"))).hasToString(
                StringUtils.leftPad("x", 45) + " "
        );
    }

    @Test
    void getDescription_without_description() {
        assertThat(new AddressWithDescription(new Address("x")).getDescription()).isEqualTo("");
    }

    @Test
    void getFormattedAddress() {
        assertThat(ADDRESS_WITH_DESCRIPTION.getFormattedAddress())
                .isEqualTo(StringUtils.leftPad(ADDRESS_WITH_DESCRIPTION.getAddress().toString(), 45));
    }

    @Test
    void getFormattedAddress_long() {
        assertThat(new AddressWithDescription(
                new Address(TOO_LONG),
                ADDRESS_WITH_DESCRIPTION.getDescription()
        ).getFormattedAddress()).isEqualTo(SHORTENED_45);
    }

    @Test
    void getFormattedDescription() {
        assertThat(ADDRESS_WITH_DESCRIPTION.getFormattedDescription())
                .isEqualTo(ADDRESS_WITH_DESCRIPTION.getDescription());
    }

    @Test
    void getFormattedDescription_long() {
        assertThat(new AddressWithDescription(
                ADDRESS_WITH_DESCRIPTION.getAddress(),
                TOO_LONG
        ).getFormattedDescription()).isEqualTo(SHORTENED_40);
    }

    @Test
    void getFormattedWithInfix() {
        assertThat(ADDRESS_WITH_DESCRIPTION.getFormattedWithInfix(Test.class)).isEqualTo(
                ADDRESS_WITH_DESCRIPTION.getFormattedAddress() +
                " interface org.junit.jupiter.api.Test " +
                ADDRESS_WITH_DESCRIPTION.getFormattedDescription()
        );
    }

    private static AddressWithDescription create(Address address, String description) {
        return new AddressWithDescription(address, description);
    }
}
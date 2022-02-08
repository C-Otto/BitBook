package de.cotto.bitbook.backend.model;

public class AddressWithDescription extends StringWithDescription<AddressWithDescription> {
    public AddressWithDescription(Address address) {
        this(address, "");
    }

    public AddressWithDescription(Address address, String description) {
        super(address.toString(), description);
    }

    @Override
    protected String getFormattedString() {
        return padOrShorten(getString(), 45);
    }

    public Address getAddress() {
        return new Address(getString());
    }

    public String getFormattedAddress() {
        return getFormattedString();
    }
}

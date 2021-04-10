package de.cotto.bitbook.backend.model;

public class AddressWithDescription extends StringWithDescription<AddressWithDescription> {
    public AddressWithDescription(String address) {
        this(address, "");
    }

    public AddressWithDescription(String address, String description) {
        super(address, description);
    }

    @Override
    protected String getFormattedString() {
        return padOrShorten(getString(), 45);
    }

    public String getAddress() {
        return getString();
    }

    public String getFormattedAddress() {
        return getFormattedString();
    }
}

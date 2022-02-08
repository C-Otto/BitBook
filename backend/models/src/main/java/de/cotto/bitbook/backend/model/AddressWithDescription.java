package de.cotto.bitbook.backend.model;

public class AddressWithDescription extends ModelWithDescription<Address, AddressWithDescription> {
    public AddressWithDescription(Address address) {
        this(address, "");
    }

    public AddressWithDescription(Address address, String description) {
        super(address, description);
    }

    @Override
    protected String getFormattedString() {
        return padOrShorten(getModel().toString(), 45);
    }

    public Address getAddress() {
        return getModel();
    }

    public String getFormattedAddress() {
        return getFormattedString();
    }
}

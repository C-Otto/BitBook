package de.cotto.bitbook.backend.model;

public record Address(String address) implements Comparable<Address> {
    public static final Address NONE = new Address("");

    public boolean isValid() {
        return !isInvalid();
    }

    public boolean isInvalid() {
        return address.isEmpty();
    }

    @Override
    public String toString() {
        return address;
    }

    @Override
    public int compareTo(Address other) {
        return address.compareTo(other.address);
    }

    public HexString getScript() {
        Base58Address base58Address = new Base58Address(address);
        if (base58Address.isValid()) {
            return base58Address.getScript();
        }
        Bech32Address bech32Address = new Bech32Address(address);
        if (bech32Address.isValid()) {
            return bech32Address.getScript();
        }
        throw new IllegalStateException("unsupported address type");
    }
}

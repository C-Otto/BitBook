package de.cotto.bitbook.ownership.persistence;

import de.cotto.bitbook.ownership.OwnershipStatus;

public class AddressOwnershipJpaDtoFixtures {
    public static final AddressOwnershipJpaDto OWNED_ADDRESS_JPA_DTO_1 =
            new AddressOwnershipJpaDto("abc", OwnershipStatus.OWNED);
    public static final AddressOwnershipJpaDto FOREIGN_ADDRESS_JPA_DTO_1 =
            new AddressOwnershipJpaDto("def", OwnershipStatus.FOREIGN);
    public static final AddressOwnershipJpaDto FOREIGN_ADDRESS_JPA_DTO_2 =
            new AddressOwnershipJpaDto("foo", OwnershipStatus.FOREIGN);
    public static final AddressOwnershipJpaDto OWNED_ADDRESS_JPA_DTO_2 =
            new AddressOwnershipJpaDto("xyz", OwnershipStatus.OWNED);
}

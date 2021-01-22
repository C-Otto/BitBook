package de.cotto.bitbook.backend;

import de.cotto.bitbook.backend.model.AddressWithDescription;

import java.util.Set;

public interface AddressWithDescriptionDao {
    AddressWithDescription get(String address);

    void save(AddressWithDescription addressWithDescription);

    Set<AddressWithDescription> findWithDescriptionInfix(String infix);

    void remove(String address);
}

package de.cotto.bitbook.backend.persistence;

import de.cotto.bitbook.backend.model.AddressWithDescription;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "AddressWithDescription")
public class AddressWithDescriptionJpaDto {
    @Id
    @Nullable
    private String address;

    @Nullable
    private String description;

    public AddressWithDescriptionJpaDto() {
        // for JPA
    }

    public AddressWithDescriptionJpaDto(@Nonnull String address, @Nonnull String description) {
        this.address = address;
        this.description = description;
    }

    public static AddressWithDescriptionJpaDto fromModel(AddressWithDescription model) {
        return new AddressWithDescriptionJpaDto(model.getAddress(), model.getDescription());
    }

    public AddressWithDescription toModel() {
        if (description == null) {
            return new AddressWithDescription(getAddress());
        }
        return new AddressWithDescription(getAddress(), getDescription());
    }

    public String getAddress() {
        return Objects.requireNonNull(address);
    }

    public String getDescription() {
        return Objects.requireNonNull(description);
    }
}

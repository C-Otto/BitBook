package de.cotto.bitbook.backend.transaction.persistence;

import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Input;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "inputs", indexes = @Index(columnList = "sourceAddress"))
public class InputJpaDto implements InputOutputJpaDto {
    @Id
    @GeneratedValue
    @SuppressWarnings({"unused", "PMD.ShortVariable"})
    private long id;

    private long value;

    @Nullable
    private String sourceAddress;

    public InputJpaDto() {
        // for JPA
    }

    public static InputJpaDto fromModel(Input output) {
        InputJpaDto dto = new InputJpaDto();
        dto.setSourceAddress(output.getAddress());
        dto.setValue(output.getValue().getSatoshis());
        return dto;
    }

    public Input toModel() {
        return new Input(Coins.ofSatoshis(value), requireNonNull(sourceAddress));
    }

    public void setValue(long value) {
        this.value = value;
    }

    public void setSourceAddress(@Nonnull String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    @Override
    public String getAddress() {
        return requireNonNull(sourceAddress);
    }
}

package de.cotto.bitbook.backend.transaction.persistence;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Output;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "outputs", indexes = @Index(columnList = "targetAddress"))
public class OutputJpaDto implements InputOutputJpaDto {
    @Id
    @GeneratedValue
    @SuppressWarnings({"unused", "PMD.ShortVariable"})
    private long id;

    private long value;

    @Nullable
    private String targetAddress;

    public OutputJpaDto() {
        // for JPA
    }

    public static OutputJpaDto fromModel(Output output) {
        OutputJpaDto dto = new OutputJpaDto();
        dto.setTargetAddress(output.getAddress().toString());
        dto.setValue(output.getValue().satoshis());
        return dto;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public Output toModel() {
        return new Output(Coins.ofSatoshis(value), new Address(requireNonNull(targetAddress)));
    }

    public void setTargetAddress(@Nonnull String targetAddress) {
        this.targetAddress = targetAddress;
    }

    @Override
    public String getAddress() {
        return requireNonNull(targetAddress);
    }
}

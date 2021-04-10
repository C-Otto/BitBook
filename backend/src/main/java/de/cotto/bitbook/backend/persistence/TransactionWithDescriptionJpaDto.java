package de.cotto.bitbook.backend.persistence;

import de.cotto.bitbook.backend.model.TransactionWithDescription;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "TransactionWithDescription")
public class TransactionWithDescriptionJpaDto {
    @Id
    @Nullable
    private String transactionHash;

    @Nullable
    private String description;

    public TransactionWithDescriptionJpaDto() {
        // for JPA
    }

    public TransactionWithDescriptionJpaDto(@Nonnull String transactionHash, @Nonnull String description) {
        this.transactionHash = transactionHash;
        this.description = description;
    }

    public static TransactionWithDescriptionJpaDto fromModel(TransactionWithDescription model) {
        return new TransactionWithDescriptionJpaDto(model.getTransactionHash(), model.getDescription());
    }

    public TransactionWithDescription toModel() {
        if (description == null) {
            return new TransactionWithDescription(getTransactionHash());
        }
        return new TransactionWithDescription(getTransactionHash(), getDescription());
    }

    public String getTransactionHash() {
        return Objects.requireNonNull(transactionHash);
    }

    public String getDescription() {
        return Objects.requireNonNull(description);
    }
}

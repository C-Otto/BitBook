package de.cotto.bitbook.backend.transaction.persistence;

import com.google.common.annotations.VisibleForTesting;
import de.cotto.bitbook.backend.model.AddressTransactions;

import javax.annotation.Nullable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "address_transactions")
public class AddressTransactionsJpaDto {
    @Id
    @Nullable
    private String address;

    @Nullable
    @ElementCollection
    private Set<String> transactionHashes;

    private int lastCheckedAtBlockheight;

    public AddressTransactionsJpaDto() {
        // for JPA
    }

    public static AddressTransactionsJpaDto fromModel(AddressTransactions addressTransactions) {
        AddressTransactionsJpaDto dto = new AddressTransactionsJpaDto();
        dto.setAddress(addressTransactions.getAddress());
        dto.setTransactionHashes(addressTransactions.getTransactionHashes());
        dto.setLastCheckedAtBlockheight(addressTransactions.getLastCheckedAtBlockHeight());
        return dto;
    }

    public AddressTransactions toModel() {
        return new AddressTransactions(
                requireNonNull(address),
                requireNonNull(transactionHashes),
                lastCheckedAtBlockheight
        );
    }

    @VisibleForTesting
    protected String getAddress() {
        return requireNonNull(address);
    }

    @VisibleForTesting
    protected Set<String> getTransactionHashes() {
        return requireNonNull(transactionHashes);
    }

    @VisibleForTesting
    protected int getLastCheckedAtBlockheight() {
        return lastCheckedAtBlockheight;
    }

    @VisibleForTesting
    protected void setAddress(@Nullable String address) {
        this.address = address;
    }

    @VisibleForTesting
    protected void setTransactionHashes(@Nullable Set<String> transactionHashes) {
        this.transactionHashes = transactionHashes;
    }

    @VisibleForTesting
    protected void setLastCheckedAtBlockheight(int lastCheckedAtBlockheight) {
        this.lastCheckedAtBlockheight = lastCheckedAtBlockheight;
    }
}

package de.cotto.bitbook.backend.transaction.persistence;

import com.google.common.annotations.VisibleForTesting;
import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.TransactionHash;

import javax.annotation.Nullable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

@Entity
@IdClass(AddressTransactionsJpaDtoId.class)
@Table(name = "address_transactions")
public class AddressTransactionsJpaDto {
    @Id
    @Nullable
    private String address;

    @Id
    @Nullable
    private String chain;

    @Nullable
    @ElementCollection
    private Set<String> transactionHashes;

    private int lastCheckedAtBlockheight;

    public AddressTransactionsJpaDto() {
        // for JPA
    }

    public static AddressTransactionsJpaDto fromModel(AddressTransactions addressTransactions) {
        AddressTransactionsJpaDto dto = new AddressTransactionsJpaDto();
        dto.setAddress(addressTransactions.address().toString());
        dto.setChain(addressTransactions.chain().toString());
        dto.setTransactionHashes(
                addressTransactions.transactionHashes().stream()
                        .map(TransactionHash::toString)
                        .collect(toSet())
        );
        dto.setLastCheckedAtBlockheight(addressTransactions.lastCheckedAtBlockHeight());
        return dto;
    }

    public AddressTransactions toModel() {
        return new AddressTransactions(
                new Address(requireNonNull(address)),
                requireNonNull(transactionHashes).stream().map(TransactionHash::new).collect(toSet()),
                lastCheckedAtBlockheight,
                Chain.valueOf(requireNonNull(chain))
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
    protected void setChain(@Nullable String chain) {
        this.chain = chain;
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

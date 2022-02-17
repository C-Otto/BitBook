package de.cotto.bitbook.backend.transaction.persistence;

import com.google.common.annotations.VisibleForTesting;
import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Input;
import de.cotto.bitbook.backend.model.Output;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Entity
@IdClass(TransactionJpaDtoId.class)
@Table(name = "transactions")
class TransactionJpaDto {
    @Id
    @Nullable
    private String hash;

    @Id
    @Nullable
    private String chain;

    private int blockHeight;

    private long time;

    private long fees;

    @Nullable
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    private List<OutputJpaDto> outputs;

    @Nullable
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    private List<InputJpaDto> inputs;

    TransactionJpaDto() {
        // for JPA
    }

    protected static TransactionJpaDto fromModel(Transaction transaction) {
        TransactionJpaDto dto = new TransactionJpaDto();
        dto.setHash(transaction.getHash().toString());
        dto.setBlockHeight(transaction.getBlockHeight());
        dto.setTime(transaction.getTime().toEpochSecond(ZoneOffset.UTC));
        dto.setFees(transaction.getFees().getSatoshis());
        dto.setInputs(transaction.getInputs().stream().map(InputJpaDto::fromModel).collect(toList()));
        dto.setOutputs(transaction.getOutputs().stream().map(OutputJpaDto::fromModel).collect(toList()));
        dto.setChain(transaction.getChain().toString());
        return dto;
    }

    protected Transaction toModel() {
        List<Input> inputModels = requireNonNull(inputs).stream()
                .map(InputJpaDto::toModel)
                .collect(toList());
        List<Output> outputModels = requireNonNull(outputs).stream()
                .map(OutputJpaDto::toModel)
                .collect(toList());
        return new Transaction(
                new TransactionHash(requireNonNull(hash)),
                blockHeight,
                LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC),
                Coins.ofSatoshis(fees),
                inputModels,
                outputModels,
                Chain.valueOf(requireNonNull(chain))
        );
    }

    @VisibleForTesting
    protected String getHash() {
        return requireNonNull(hash);
    }

    @VisibleForTesting
    protected void setHash(@Nonnull String hash) {
        this.hash = hash;
    }

    @VisibleForTesting
    protected int getBlockHeight() {
        return blockHeight;
    }

    @VisibleForTesting
    protected void setBlockHeight(int blockHeight) {
        this.blockHeight = blockHeight;
    }

    @VisibleForTesting
    protected void setTime(long time) {
        this.time = time;
    }

    @VisibleForTesting
    protected long getTime() {
        return time;
    }

    @VisibleForTesting
    protected void setFees(long fees) {
        this.fees = fees;
    }

    @VisibleForTesting
    protected void setInputs(@Nonnull List<InputJpaDto> inputs) {
        this.inputs = inputs;
    }

    @VisibleForTesting
    protected void setOutputs(@Nonnull List<OutputJpaDto> outputs) {
        this.outputs = outputs;
    }

    @VisibleForTesting
    protected void setChain(@Nonnull String chain) {
        this.chain = chain;
    }
}

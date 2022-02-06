package de.cotto.bitbook.backend.transaction.deserialization;

import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Input;
import de.cotto.bitbook.backend.model.Output;
import de.cotto.bitbook.backend.model.Transaction;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class TransactionDto {
    private final String hash;
    private final int blockHeight;
    private final LocalDateTime time;
    private final long fees;
    private final List<InputDto> inputs;
    private final List<OutputDto> outputs;

    public TransactionDto(
            String hash,
            int blockHeight,
            LocalDateTime time,
            long fees,
            List<InputDto> inputs,
            List<OutputDto> outputs
    ) {
        this.hash = hash;
        this.blockHeight = blockHeight;
        this.time = time;
        this.fees = fees;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public Transaction toModel() {
        List<Output> outputModels = outputs.stream().map(OutputDto::toModel).collect(toList());
        if (inputs.size() == 1 && InputDto.COINBASE.equals(inputs.get(0))) {
            return Transaction.forCoinbase(hash, blockHeight, time, Coins.ofSatoshis(fees), outputModels);
        }
        List<Input> inputModels = inputs.stream().map(InputDto::toModel).collect(toList());
        return new Transaction(hash, blockHeight, time, Coins.ofSatoshis(fees), inputModels, outputModels);
    }
}

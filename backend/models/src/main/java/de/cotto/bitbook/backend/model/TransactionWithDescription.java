package de.cotto.bitbook.backend.model;

public class TransactionWithDescription extends ModelWithDescription<TransactionHash, TransactionWithDescription> {
    public TransactionWithDescription(TransactionHash transactionHash) {
        this(transactionHash, "");
    }

    public TransactionWithDescription(TransactionHash transactionHash, String description) {
        super(transactionHash, description);
    }

    @Override
    protected String getFormattedString() {
        return getModel().toString();
    }

    public TransactionHash getTransactionHash() {
        return getModel();
    }
}

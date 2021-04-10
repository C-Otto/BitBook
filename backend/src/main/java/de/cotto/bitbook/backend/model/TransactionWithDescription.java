package de.cotto.bitbook.backend.model;

public class TransactionWithDescription extends StringWithDescription<TransactionWithDescription> {
    public TransactionWithDescription(String transactionHash) {
        this(transactionHash, "");
    }

    public TransactionWithDescription(String transactionHash, String description) {
        super(transactionHash, description);
    }

    @Override
    protected String getFormattedString() {
        return getString();
    }

    public String getTransactionHash() {
        return getString();
    }
}

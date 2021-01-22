package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import de.cotto.bitbook.backend.transaction.model.Coins;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BalanceService {
    private final AddressTransactionsService addressTransactionsService;
    private final TransactionService transactionService;

    public BalanceService(
            AddressTransactionsService addressTransactionsService,
            TransactionService transactionService
    ) {
        this.addressTransactionsService = addressTransactionsService;
        this.transactionService = transactionService;
    }

    public Coins getBalance(String address) {
        AddressTransactions transactions = addressTransactionsService.getTransactions(address);
        Set<String> transactionHashes = transactions.getTransactionHashes();
        return transactionService.getTransactionDetails(transactionHashes).stream()
                .map(transactionDetails -> {
                    Coins incoming = transactionDetails.getIncomingCoins(address);
                    Coins outgoing = transactionDetails.getOutgoingCoins(address);
                    return Coins.NONE.add(incoming).subtract(outgoing);
                })
                .reduce(Coins.NONE, Coins::add);
    }

}

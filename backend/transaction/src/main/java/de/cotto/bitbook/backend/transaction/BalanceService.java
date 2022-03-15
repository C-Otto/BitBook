package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.Chain;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.TransactionHash;
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

    public Coins getBalance(Address address, Chain chain) {
        AddressTransactions transactions = addressTransactionsService.getTransactions(address, chain);
        Set<TransactionHash> transactionHashes = transactions.transactionHashes();
        return transactionService.getTransactionDetails(transactionHashes, chain).stream()
                .map(transactionDetails -> transactionDetails.getDifferenceForAddress(address))
                .reduce(Coins.NONE, Coins::add);
    }

}

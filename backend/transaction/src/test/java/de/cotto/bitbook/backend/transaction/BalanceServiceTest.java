package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Input;
import de.cotto.bitbook.backend.transaction.model.Output;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {
    @InjectMocks
    private BalanceService balanceService;

    @Mock
    private AddressTransactionsService addressTransactionsService;

    @Mock
    private TransactionService transactionService;

    @Test
    void getBalance_two_incoming_transactions() {
        Transaction transaction1 = getTransaction(124, 1, Map.of(123, ADDRESS));
        Transaction transaction2 = getTransaction(456, 0, Map.of(400, ADDRESS, 56, "z"));

        when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2)))
                .thenReturn(Set.of(transaction1, transaction2));
        when(addressTransactionsService.getTransactions(ADDRESS)).thenReturn(ADDRESS_TRANSACTIONS);

        Coins balance = balanceService.getBalance(ADDRESS);

        assertThat(balance).isEqualTo(Coins.ofSatoshis(123 + 400));
    }

    private Transaction getTransaction(int inputAmount, int fee, Map<Integer, String> outputsToAddresses) {
        List<Output> outputs = outputsToAddresses.entrySet().stream()
                .map(entry -> new Output(Coins.ofSatoshis(entry.getKey()), entry.getValue()))
                .collect(toList());
        List<Input> inputs = List.of(new Input(Coins.ofSatoshis(inputAmount), "z"));
        return new Transaction("x", BLOCK_HEIGHT, DATE_TIME, Coins.ofSatoshis(fee), inputs, outputs);
    }
}
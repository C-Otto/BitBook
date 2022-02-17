package de.cotto.bitbook.backend.transaction;

import de.cotto.bitbook.backend.model.Address;
import de.cotto.bitbook.backend.model.AddressTransactions;
import de.cotto.bitbook.backend.model.Coins;
import de.cotto.bitbook.backend.model.Input;
import de.cotto.bitbook.backend.model.Output;
import de.cotto.bitbook.backend.model.Transaction;
import de.cotto.bitbook.backend.model.TransactionHash;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.cotto.bitbook.backend.model.AddressFixtures.ADDRESS;
import static de.cotto.bitbook.backend.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.model.Chain.BCH;
import static de.cotto.bitbook.backend.model.Chain.BTC;
import static de.cotto.bitbook.backend.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.model.TransactionHashFixtures.TRANSACTION_HASH_2;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
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
        Transaction transaction2 = getTransaction(456, 0, Map.of(400, ADDRESS, 56, new Address("z")));

        when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2), BTC))
                .thenReturn(Set.of(transaction1, transaction2));
        when(addressTransactionsService.getTransactions(ADDRESS, BTC)).thenReturn(ADDRESS_TRANSACTIONS);

        Coins balance = balanceService.getBalance(ADDRESS, BTC);

        assertThat(balance).isEqualTo(Coins.ofSatoshis(123 + 400));
    }

    @Test
    void getBalance_bch() {
        when(addressTransactionsService.getTransactions(ADDRESS, BCH)).thenReturn(AddressTransactions.unknown(BCH));
        balanceService.getBalance(ADDRESS, BCH);
        verify(addressTransactionsService).getTransactions(ADDRESS, BCH);
    }

    private Transaction getTransaction(int inputAmount, int fee, Map<Integer, Address> outputsToAddresses) {
        List<Output> outputs = outputsToAddresses.entrySet().stream()
                .map(entry -> new Output(Coins.ofSatoshis(entry.getKey()), entry.getValue()))
                .collect(toList());
        List<Input> inputs = List.of(new Input(Coins.ofSatoshis(inputAmount), new Address("z")));
        return new Transaction(
                new TransactionHash("x"),
                BLOCK_HEIGHT,
                DATE_TIME,
                Coins.ofSatoshis(fee),
                inputs,
                outputs,
                BTC
        );
    }
}
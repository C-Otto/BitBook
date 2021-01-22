package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.model.AddressWithDescription;
import de.cotto.bitbook.backend.transaction.AddressTransactionsService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Input;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS_UPDATED;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.LAST_CHECKED_AT_BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.TRANSACTION_HASH_3;
import static de.cotto.bitbook.backend.transaction.model.InputFixtures.INPUT_ADDRESS_1;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.DATE_TIME;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_2;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH;
import static de.cotto.bitbook.backend.transaction.model.TransactionFixtures.TRANSACTION_HASH_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionsCommandsTest {
    private static final String EXPECTED = "expected";

    @InjectMocks
    private TransactionsCommands transactionsCommands;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AddressTransactionsService addressTransactionsService;

    @Mock
    private AddressDescriptionService addressDescriptionService;

    @Mock
    private TransactionFormatter transactionFormatter;

    @Mock
    private AddressFormatter addressFormatter;

    @Test
    void getTransactionDetails() {
        when(transactionService.getTransactionDetails(TRANSACTION_HASH)).thenReturn(TRANSACTION);
        when(transactionFormatter.format(TRANSACTION)).thenReturn(EXPECTED);
        String details = transactionsCommands.getTransactionDetails(TRANSACTION_HASH);
        assertThat(details).isEqualTo(EXPECTED);
    }

    @Test
    void getTransactionDetails_removes_non_hex_characters() {
        when(transactionService.getTransactionDetails(TRANSACTION_HASH)).thenReturn(TRANSACTION);
        when(transactionFormatter.format(TRANSACTION)).thenReturn(EXPECTED);
        String details = transactionsCommands.getTransactionDetails(" x" + TRANSACTION_HASH + ":,! ");
        assertThat(details).isEqualTo(EXPECTED);
    }

    @Test
    void getTransactionDetails_too_short() {
        assertThat(transactionsCommands.getTransactionDetails("abc"))
                .isEqualTo("Expected: 64 hex characters");
        verifyNoInteractions(transactionService);
    }

    @Test
    void getAddressTransactions_wrong_address() {
        assertThat(transactionsCommands.getAddressTransactions(new CliAddress("xxx")))
                .isEqualTo(CliAddress.ERROR_MESSAGE);
    }

    @Test
    void getAddressTransactions_unknown_transaction_returned_for_two_hashes() {
        prepareMocks();
        when(transactionService.getTransactionDetails(anySet()))
                .thenReturn(Set.of(Transaction.UNKNOWN, TRANSACTION, TRANSACTION_2));
        when(addressTransactionsService.getTransactions(any())).thenReturn(ADDRESS_TRANSACTIONS_UPDATED);

        String details = transactionsCommands.getAddressTransactions(new CliAddress(ADDRESS));

        assertThat(details)
                .startsWith("Address: " + ADDRESS + " ?\nTransaction hashes (4):")
                .endsWith("\n[Details for at least one transaction could not be downloaded]")
                .contains(TRANSACTION_HASH)
                .contains(TRANSACTION_HASH_2);
    }

    @Test
    void getAddressTransactions_sorted_by_absolute_difference_for_transaction() {
        String address = INPUT_ADDRESS_1;
        Transaction transaction3 = new Transaction(
                TRANSACTION_HASH_3,
                BLOCK_HEIGHT,
                DATE_TIME,
                Coins.ofSatoshis(1_000_000),
                List.of(new Input(Coins.ofSatoshis(1_000_000), address)),
                List.of()
        );
        when(addressDescriptionService.get(address)).thenReturn(new AddressWithDescription(address));
        when(addressFormatter.getFormattedOwnershipStatus(address)).thenReturn("?");
        AddressTransactions addressTransactions1 = new AddressTransactions(
                ADDRESS,
                Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2, TRANSACTION_HASH_3),
                LAST_CHECKED_AT_BLOCK_HEIGHT
        );
        when(addressTransactionsService.getTransactions(address)).thenReturn(addressTransactions1);
        when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2, TRANSACTION_HASH_3)))
                .thenReturn(Set.of(TRANSACTION, TRANSACTION_2, transaction3));

        when(transactionFormatter.formatSingleLineForAddress(TRANSACTION, address)).thenReturn("f1");
        when(transactionFormatter.formatSingleLineForAddress(TRANSACTION_2, address)).thenReturn("f2");
        when(transactionFormatter.formatSingleLineForAddress(transaction3, address)).thenReturn("f3");
        String addressTransactions = transactionsCommands.getAddressTransactions(new CliAddress(address));

        assertThat(addressTransactions).isEqualTo("""
                Address: bc1xxxn59nfqcw2la4ms7zsphqllm5789syhrgcupw ?
                Transaction hashes (3):
                f2
                f1
                f3"""
        );
    }

    @Test
    void getAddressTransactions_with_description() {
        when(addressFormatter.getFormattedOwnershipStatus(ADDRESS)).thenReturn("?");
        when(addressDescriptionService.get(ADDRESS))
                .thenReturn(new AddressWithDescription(ADDRESS, "description"));
        when(addressTransactionsService.getTransactions(ADDRESS)).thenReturn(ADDRESS_TRANSACTIONS);
        when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2)))
                .thenReturn(Set.of(TRANSACTION, TRANSACTION_2));
        String addressTransactions = transactionsCommands.getAddressTransactions(new CliAddress(ADDRESS));

        assertThat(addressTransactions).startsWith("Address: 1DEP8i3QJCsomS4BSMY2RpU1upv62aGvhD ? (description)");
    }

    private void prepareMocks() {
        when(addressDescriptionService.get(any()))
                .then(invocation -> new AddressWithDescription(invocation.getArgument(0)));
        when(addressFormatter.getFormattedOwnershipStatus(ADDRESS)).thenReturn("?");
        when(transactionFormatter.formatSingleLineForAddress(any(), any()))
                .then(invocation -> invocation.getArgument(0) + "/" + invocation.getArgument(1));
    }
}
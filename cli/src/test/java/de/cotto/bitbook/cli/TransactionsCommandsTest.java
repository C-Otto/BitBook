package de.cotto.bitbook.cli;

import de.cotto.bitbook.backend.AddressDescriptionService;
import de.cotto.bitbook.backend.TransactionDescriptionService;
import de.cotto.bitbook.backend.price.PriceService;
import de.cotto.bitbook.backend.transaction.AddressTransactionsService;
import de.cotto.bitbook.backend.transaction.TransactionService;
import de.cotto.bitbook.backend.transaction.model.AddressTransactions;
import de.cotto.bitbook.backend.transaction.model.Coins;
import de.cotto.bitbook.backend.transaction.model.Input;
import de.cotto.bitbook.backend.transaction.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS;
import static de.cotto.bitbook.backend.transaction.model.AddressTransactionsFixtures.ADDRESS_TRANSACTIONS;
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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionsCommandsTest {
    private static final String EXPECTED = "expected";
    private static final String DESCRIPTION = "some description";

    private static final CliAddress CLI_ADDRESS_INVALID = new CliAddress("y");
    private static final CliTransactionHash CLI_TRANSACTION_HASH = new CliTransactionHash(TRANSACTION_HASH);
    private static final CliTransactionHash CLI_TRANSACTION_HASH_INVALID = new CliTransactionHash("x");

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

    @Mock
    private TransactionDescriptionService transactionDescriptionService;

    @Mock
    private PriceService priceService;

    @Test
    void getTransactionDetails() {
        when(transactionService.getTransactionDetails(TRANSACTION_HASH)).thenReturn(TRANSACTION);
        when(transactionFormatter.format(TRANSACTION)).thenReturn(EXPECTED);
        String details = transactionsCommands.getTransactionDetails(CLI_TRANSACTION_HASH);
        assertThat(details).isEqualTo(EXPECTED);
    }

    @Test
    void getTransactionDetails_invalid() {
        assertThat(transactionsCommands.getTransactionDetails(CLI_TRANSACTION_HASH_INVALID))
                .isEqualTo("Expected: 64 hex characters");
        verifyNoInteractions(transactionService);
    }

    @Test
    void getAddressTransactions_wrong_address() {
        assertThat(transactionsCommands.getAddressTransactions(CLI_ADDRESS_INVALID))
                .isEqualTo(CliAddress.ERROR_MESSAGE);
    }

    @Test
    void getAddressTransactions_unknown_transaction_returned_for_two_hashes() {
        prepareMocks();
        when(transactionService.getTransactionDetails(anySet()))
                .thenReturn(Set.of(Transaction.UNKNOWN, TRANSACTION_2));
        when(addressTransactionsService.getTransactions(any())).thenReturn(ADDRESS_TRANSACTIONS);

        String details = transactionsCommands.getAddressTransactions(new CliAddress(ADDRESS));

        assertThat(details)
                .startsWith("Address: " + ADDRESS + " ?\nDescription: \nTransaction hashes (2):")
                .contains(TRANSACTION_HASH_2)
                .endsWith("\n[Details for at least one transaction could not be downloaded]");
    }

    @Test
    void getAddressTransactions_does_not_return_all_requested_transactions() {
        prepareMocks();
        when(transactionService.getTransactionDetails(anySet()))
                .thenReturn(Set.of(TRANSACTION));
        when(addressTransactionsService.getTransactions(any())).thenReturn(ADDRESS_TRANSACTIONS);

        String details = transactionsCommands.getAddressTransactions(new CliAddress(ADDRESS));

        assertThat(details)
                .startsWith("Address: " + ADDRESS + " ?\nDescription: \nTransaction hashes (2):")
                .contains(TRANSACTION_HASH)
                .endsWith("\n[Details for at least one transaction could not be downloaded]");
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
        when(addressDescriptionService.getDescription(address)).thenReturn("");
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
                Description:\040
                Transaction hashes (3):
                f2
                f1
                f3"""
        );
    }

    @Test
    void getAddressTransactions_with_description() {
        when(addressFormatter.getFormattedOwnershipStatus(ADDRESS)).thenReturn("?");
        when(addressDescriptionService.getDescription(ADDRESS)).thenReturn("description");
        when(addressTransactionsService.getTransactions(ADDRESS)).thenReturn(ADDRESS_TRANSACTIONS);
        when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH, TRANSACTION_HASH_2)))
                .thenReturn(Set.of(TRANSACTION, TRANSACTION_2));

        String addressTransactions = transactionsCommands.getAddressTransactions(new CliAddress(ADDRESS));

        assertThat(addressTransactions)
                .startsWith("Address: 1DEP8i3QJCsomS4BSMY2RpU1upv62aGvhD ?\nDescription: description\n");
    }

    @Test
    void getAddressTransactions_requests_all_prices_before_formatting_details() {
        when(addressTransactionsService.getTransactions(ADDRESS))
                .thenReturn(new AddressTransactions(ADDRESS, Set.of(TRANSACTION_HASH), LAST_CHECKED_AT_BLOCK_HEIGHT));
        when(transactionService.getTransactionDetails(Set.of(TRANSACTION_HASH))).thenReturn(Set.of(TRANSACTION));

        transactionsCommands.getAddressTransactions(new CliAddress(ADDRESS));

        InOrder inOrder = inOrder(priceService, transactionFormatter);
        inOrder.verify(priceService).getPrice(TRANSACTION.getTime());
        inOrder.verify(transactionFormatter).formatSingleLineForAddress(any(), any());
    }

    @Test
    void setTransactionDescription() {
        String result = transactionsCommands.setTransactionDescription(CLI_TRANSACTION_HASH, DESCRIPTION);
        assertThat(result).isEqualTo("OK");
        verify(transactionDescriptionService).set(TRANSACTION_HASH, DESCRIPTION);
    }

    @Test
    void setTransactionDescription_invalid() {
        String result = transactionsCommands.setTransactionDescription(CLI_TRANSACTION_HASH_INVALID, DESCRIPTION);
        assertThat(result).isEqualTo(CliTransactionHash.ERROR_MESSAGE);
        verifyNoInteractions(transactionDescriptionService);
    }

    @Test
    void removeTransactionDescription() {
        String result = transactionsCommands.removeTransactionDescription(CLI_TRANSACTION_HASH);
        assertThat(result).isEqualTo("OK");
        verify(transactionDescriptionService).remove(TRANSACTION_HASH);
    }

    @Test
    void removeTransactionDescription_invalid() {
        String result = transactionsCommands.removeTransactionDescription(CLI_TRANSACTION_HASH_INVALID);
        assertThat(result).isEqualTo(CliTransactionHash.ERROR_MESSAGE);
        verifyNoInteractions(transactionDescriptionService);
    }

    private void prepareMocks() {
        when(addressDescriptionService.getDescription(any())).thenReturn("");
        when(addressFormatter.getFormattedOwnershipStatus(ADDRESS)).thenReturn("?");
        when(transactionFormatter.formatSingleLineForAddress(any(), any()))
                .then(invocation -> invocation.getArgument(0) + "/" + invocation.getArgument(1));
    }
}